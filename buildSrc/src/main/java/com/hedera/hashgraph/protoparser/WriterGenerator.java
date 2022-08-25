package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Lexer;
import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.hedera.hashgraph.protoparser.Common.MODELS_DEST_PACKAGE;
import static com.hedera.hashgraph.protoparser.Common.SCHEMAS_DEST_PACKAGE;
import static com.hedera.hashgraph.protoparser.Common.WRITERS_DEST_PACKAGE;
import static com.hedera.hashgraph.protoparser.Common.camelToUpperSnake;
import static com.hedera.hashgraph.protoparser.Common.capitalizeFirstLetter;
import static com.hedera.hashgraph.protoparser.Common.computeJavaPackage;
import static com.hedera.hashgraph.protoparser.SchemaGenerator.SCHEMA_JAVA_FILE_SUFFIX;

/**
 * Code generator that parses protobuf files and generates schemas for each message type.
 */
public class WriterGenerator {

	/** Suffix for schema java classes */
	public static final String WRITER_JAVA_FILE_SUFFIX = "Writer";

	/** Record for a enum value tempory storage */
	private record EnumValue(String name, boolean deprecated, String javaDoc) {}
	/** Record for a field doc tempory storage */
	private record FieldDoc(String fieldName, String fieldComment) {}

	/**
	 * Main generate method that process directory of protovuf files
	 *
	 * @param protoDir The protobuf file to parse
	 * @param destinationSrcDir the generated source directory to write files into
	 * @param lookupHelper helper for global context
	 * @throws IOException if there was a problem writing files
	 */
	static void generateWriters(File protoDir, File destinationSrcDir, final LookupHelper lookupHelper) throws IOException {
		generate(protoDir, destinationSrcDir,lookupHelper);
	}


	/**
	 * Process a directory of protobuf files or indervidual protobuf file. Generating Java record clasess for each
	 * message type and Java enums for each protobuf enum.
	 *
	 * @param protoDirOrFile directory of protobuf files or indervidual protobuf file
	 * @param destinationSrcDir The destination source directory to generate into
	 * @param lookupHelper helper for global context
	 * @throws IOException if there was a problem writing generated files
	 */
	private static void generate(File protoDirOrFile, File destinationSrcDir,
			final LookupHelper lookupHelper) throws IOException {
		if (protoDirOrFile.isDirectory()) {
			for (final File file : protoDirOrFile.listFiles()) {
				if (file.isDirectory() || file.getName().endsWith(".proto")) {
					generate(file, destinationSrcDir, lookupHelper);
				}
			}
		} else {
			final String dirName = protoDirOrFile.getParentFile().getName().toLowerCase();
			try (var input = new FileInputStream(protoDirOrFile)) {
				final var lexer = new Protobuf3Lexer(CharStreams.fromStream(input));
				final var parser = new Protobuf3Parser(new CommonTokenStream(lexer));
				final Protobuf3Parser.ProtoContext parsedDoc = parser.proto();
				final String javaPackage = computeJavaPackage(WRITERS_DEST_PACKAGE, dirName);
				final Path packageDir = destinationSrcDir.toPath().resolve(javaPackage.replace('.', '/'));
				Files.createDirectories(packageDir);
				for (var topLevelDef : parsedDoc.topLevelDef()) {
					final Protobuf3Parser.MessageDefContext msgDef = topLevelDef.messageDef();
					if (msgDef != null) {
						generateWriterFile(msgDef, dirName, javaPackage, packageDir, lookupHelper);
					}
				}
			}
		}
	}

	/**
	 * Generate a Java writer class from protobuf message type
	 *
	 * @param msgDef the parsed message
	 * @param dirName the directory name of the dir containing the protobuf file
	 * @param javaPackage the java package the writer file should be generated in
	 * @param packageDir the output package directory
	 * @param lookupHelper helper for global context
	 * @throws IOException If there was a problem writing record file
	 */
	private static void generateWriterFile(Protobuf3Parser.MessageDefContext msgDef, String dirName, String javaPackage,
			Path packageDir, final LookupHelper lookupHelper) throws IOException {
		final var modelClassName = msgDef.messageName().getText();
		final var schemaClassName = modelClassName+ SCHEMA_JAVA_FILE_SUFFIX;
		final var writerClassName = modelClassName+ WRITER_JAVA_FILE_SUFFIX;
		final var javaFile = packageDir.resolve(writerClassName + ".java");
		String javaDocComment = (msgDef.docComment()== null) ? "" :
				msgDef.docComment().getText()
						.replaceAll("\n \\*\s*\n","\n * <p>\n");
		String deprectaed = "";
		final List<Field> fields = new ArrayList<>();
		final Set<String> imports = new TreeSet<>();
		imports.add(computeJavaPackage(MODELS_DEST_PACKAGE, dirName));
		imports.add(computeJavaPackage(SCHEMAS_DEST_PACKAGE, dirName));
		for(var item: msgDef.messageBody().messageElement()) {
			if (item.messageDef() != null) { // process sub messages
				generateWriterFile(item.messageDef(), dirName, javaPackage,packageDir,lookupHelper);
			} else if (item.oneof() != null) { // process one ofs
				final var field = new OneOfField(item.oneof(), modelClassName, lookupHelper);
				fields.add(field);
				field.addAllNeededImports(imports, true, false, true);
			} else if (item.mapField() != null) { // process map fields
				throw new IllegalStateException("Encountered a mapField that was not handled in "+ writerClassName);
			} else if (item.reserved() != null) { // process reserved
				// reserved are not needed
			} else if (item.field() != null && item.field().fieldName() != null) {
				final var field = new SingleField(item.field(), lookupHelper);
				fields.add(field);
				if (field.type() == Field.FieldType.MESSAGE) {
					field.addAllNeededImports(imports, true, false, true);
				}
			} else if (item.optionStatement() != null){
				// no needed for now
			} else {
				System.err.println("Unknown Element: "+item+" -- "+item.getText());
			}
		}
		final List<Field> sortedFields = fields.stream()
				.sorted((a,b) -> Integer.compare(a.fieldNumber(), b.fieldNumber()))
				.collect(Collectors.toList());
		final String fieldWriteLines = generateFieldWriteLines(sortedFields, schemaClassName, imports);
		try (FileWriter javaWriter = new FileWriter(javaFile.toFile())) {
			javaWriter.write("""
					package %s;
									
					import java.io.IOException;	
					import java.io.OutputStream;
					import com.hedera.hashgraph.protoparse.ProtoOutputStream;
					%s
										
					/**
					 * Writer for %s model object. Generate based on protobuf schema.
					 */
					@SuppressWarnings({"unchecked", "OptionalAssignedToNull"})
					public final class %s {
						// -- WRITE METHODS ---------------------------------------------
						public static void write(%s data, OutputStream out) throws IOException {
							final ProtoOutputStream pout = new ProtoOutputStream(%s::valid,out);
							%s
						}
					}
					""".formatted(
						javaPackage,
						imports.isEmpty() ? "" : imports.stream()
								.filter(input -> !input.equals(javaPackage))
								.collect(Collectors.joining(".*;\nimport ","\nimport ",".*;\n")),
						modelClassName,
						writerClassName,
						modelClassName,
						schemaClassName,
						fieldWriteLines
					)
					//  final ProtoOutputStream pout = new ProtoOutputStream(%s::valid, out);
			);
		}
	}

	private static String generateFieldWriteLines(final List<Field> fields, String schemaClassName,final Set<String> imports) {
		return fields.stream()
				.map(field -> generateFieldWriteLines(field, schemaClassName, "data.%s()".formatted(field.nameCamelFirstLower()), imports))
				.collect(Collectors.joining("\n		"));
	}

	private static String generateFieldWriteLines(final Field field, final String schemaClassName, final String getValueCode, final Set<String> imports) {
		final String fieldName = field.nameCamelFirstLower();
		final String fieldDef = schemaClassName+"."+camelToUpperSnake(field.name());
		if (field instanceof OneOfField) {
			final OneOfField oneOfField = (OneOfField)field;
			final String oneOfName = field.name()+"OneOf";
			return """
					final var %s = data.%s(); 
					if(%s != null) {
						switch(%s.kind()) {
					%s
						}
					}""".formatted(
					oneOfName,fieldName,oneOfName,oneOfName,
					oneOfField.fields().stream().map(f ->
							 			"""
 							                    case %s -> {
 							                %s
 							            }"""
									.formatted(camelToUpperSnake(f.name()), generateFieldWriteLines(f,schemaClassName,"((%s)%s.as())".formatted(f.javaFieldType(),oneOfName), imports))
												.replaceAll("\n","\n        "))
							.collect(Collectors.joining("\n"))

			).replaceAll("\n","\n		");
		} else {
			final String writeMethodName = mapToWriteMethod(field);
			if(field.optional()) {
				System.out.println("OPTIONAL field = " + field);
				return switch (field.messageType()) {
					case "EnumValue" -> "if (%s != null && %s.isPresent()) pout.writeEnum(%s, %s.get().protobufOrdinal());"
							.formatted(getValueCode, getValueCode, fieldDef, getValueCode);
					case "StringValue" -> "if (%s != null && !%s.orElse(\"\").isEmpty()) pout.writeString(%s, %s.get());"
							.formatted(getValueCode, getValueCode, fieldDef,getValueCode);
					case "BoolValue" -> "if (%s != null && %s.orElse(false)) pout.writeBoolean(%s, true);"
							.formatted(getValueCode, getValueCode, fieldDef);
					case "Int32Value","UInt32Value","SInt32Value" -> "if (%s != null && %s.isPresent()) pout.writeInteger(%s, %s.get());"
							.formatted(getValueCode, getValueCode, fieldDef, getValueCode);
					case "Int64Value","UInt64Value","SInt64Value" -> "if (%s != null && %s.isPresent()) pout.writeLong(%s, %s.get());"
							.formatted(getValueCode, getValueCode, fieldDef, getValueCode);
					case "FloatValue" -> "if (%s != null && %s.isPresent()) pout.writeFloat(%s, %s.get());"
							.formatted(getValueCode, getValueCode, fieldDef, getValueCode);
					case "DoubleValue" -> "if (%s != null && %s.isPresent()) pout.writeDouble(%s, %s.get());"
							.formatted(getValueCode, getValueCode, fieldDef, getValueCode);
					case "BytesValue" -> "if (%s != null && %s.isPresent()) pout.writeBytes(%s, %s.get());"
							.formatted(getValueCode, getValueCode, fieldDef, getValueCode);
					default -> throw new UnsupportedOperationException("Unhandled optional message type:"+field.messageType());
				};
			} else if (field.repeated()) {
				return switch(field.type()) {
					case ENUM -> "if (%s != null && !%s.isEmpty()) pout.writeEnumList(%s, %s.stream().map(e -> e.protobufOrdinal()).collect(java.util.stream.Collectors.toList()));"
							.formatted(getValueCode, getValueCode, fieldDef, getValueCode);
					case MESSAGE -> "if (%s != null && !%s.isEmpty()) pout.writeMessageList(%s, %s, %s::write);"
							.formatted(getValueCode,getValueCode, fieldDef,getValueCode,
									capitalizeFirstLetter(field.messageType())+ WRITER_JAVA_FILE_SUFFIX
							);
					default -> "if (%s != null && !%s.isEmpty()) pout.write%sList(%s, %s);"
							.formatted(getValueCode, getValueCode, writeMethodName, fieldDef, getValueCode);
				};
			} else {
				return switch(field.type()) {
					case ENUM -> "if (%s != null) pout.writeEnum(%s, %s.protobufOrdinal());"
							.formatted(getValueCode, fieldDef, getValueCode);
					case STRING -> "if (%s != null && !%s.isEmpty()) pout.writeString(%s, %s);"
							.formatted(getValueCode, getValueCode,fieldDef,getValueCode);
					case MESSAGE -> "if (%s != null) pout.writeMessage(%s, %s, %s::write);"
							.formatted(getValueCode,fieldDef,getValueCode,
									capitalizeFirstLetter(field.messageType())+ WRITER_JAVA_FILE_SUFFIX
							);
					case BOOL -> "if (%s) pout.writeBoolean(%s, true);"
							.formatted(getValueCode, fieldDef);
					default -> "if (%s != %s) pout.write%s(%s, %s);"
							.formatted(getValueCode, field.javaDefault(), writeMethodName, fieldDef, getValueCode);
				};
			}
		}
	}

	private static String mapToWriteMethod(Field field) {
		String writeType = switch(field.type()) {
			case BOOL -> "Boolean";
			case INT32 -> "Integer";
			case SINT32 -> "Integer";
			case UINT32 -> "Integer";
			case INT64 -> "Long";
			case SINT64 -> "Long";
			case UINT64 -> "Long";
			case FLOAT -> "Float";
			case DOUBLE -> "Double";
			case MESSAGE -> "Message";
			case STRING -> "String";
			case ENUM -> "Enum";
			case BYTES -> "Bytes";
			default -> throw new UnsupportedOperationException("mapToWriteMethod can not handle "+field.type());
		};
		return writeType;
	}

}
