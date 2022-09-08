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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.hedera.hashgraph.protoparser.Common.*;
import static com.hedera.hashgraph.protoparser.EnumGenerator.*;
import static com.hedera.hashgraph.protoparser.EnumGenerator.EnumValue;

/**
 * Code generator that parses protobuf files and generates nice Java source for record files for each message type and
 * enum.
 */
public class ModelGenerator {
	/** Record for a field doc tempory storage */
	private record FieldDoc(String fieldName, String fieldComment) {}

	/**
	 * Main generate method that process a single protobuf file and writes records and enums into package directories
	 * inside the destinationSrcDir.
	 *
	 * @param protoFile The protobuf file to parse
	 * @param destinationSrcDir the generated source directory to write files into
	 * @throws IOException if there was a problem writing files
	 */
	static void generateModel(File protoFile, File destinationSrcDir, final LookupHelper lookupHelper) throws IOException {
		generate(protoFile, destinationSrcDir, lookupHelper);
	}

	/**
	 * Process a directory of protobuf files or indervidual protobuf file. Generating Java record clasess for each
	 * message type and Java enums for each protobuf enum.
	 *
	 * @param protoDirOrFile directory of protobuf files or indervidual protobuf file
	 * @param destinationSrcDir The destination source directory to generate into
	 * @param packageMap map of protobuf message types to java packages to use to build imports
	 * @throws IOException if there was a problem writing generated files
	 */
	private static void generate(File protoDirOrFile, File destinationSrcDir, final LookupHelper lookupHelper) throws IOException {
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
				final String javaPackage = computeJavaPackage(MODELS_DEST_PACKAGE, dirName);
				final Path packageDir = destinationSrcDir.toPath().resolve(javaPackage.replace('.', '/'));
				Files.createDirectories(packageDir);
				for (var topLevelDef : parsedDoc.topLevelDef()) {
					final Protobuf3Parser.MessageDefContext msgDef = topLevelDef.messageDef();
					if (msgDef != null) {
						generateRecordFile(msgDef, javaPackage, packageDir, lookupHelper);
					}
					final Protobuf3Parser.EnumDefContext enumDef = topLevelDef.enumDef();
					if (enumDef != null) {
						final var enumName = snakeToCamel(enumDef.enumName().getText(), true);
						final var javaFile = packageDir.resolve(enumName + ".java");
						generateEnumFile(enumDef, javaPackage,enumName, javaFile.toFile(), lookupHelper);
					}
				}
			}
		}
	}

	/**
	 * Generate a Java record from protobuf message type
	 *
	 * @param msgDef the parsed message
	 * @param javaPackage the package the record will be placed in
	 * @param packageDir the package directory for writing into
	 * @param packageMap map of message type to Java package for imports
	 * @throws IOException if there was a problem writing generated code
	 */
	private static void generateRecordFile(Protobuf3Parser.MessageDefContext msgDef, String javaPackage, Path packageDir, final LookupHelper lookupHelper) throws IOException {
		final var javaRecordName = msgDef.messageName().getText();
		final var javaFile = packageDir.resolve(javaRecordName + ".java");
		String javaDocComment = (msgDef.docComment()== null) ? "" :
				msgDef.docComment().getText()
						.replaceAll("\n \\*\s*\n","\n * <p>\n");
		String deprectaed = "";
		final List<Field> fields = new ArrayList<>();
		final List<String> oneofEnums = new ArrayList<>();
		final List<FieldDoc> fieldDocs = new ArrayList<>();
		final Set<String> imports = new TreeSet<>();
		for(var item: msgDef.messageBody().messageElement()) {
			if (item.messageDef() != null) { // process sub messages
				generateRecordFile(item.messageDef(), javaPackage,packageDir,lookupHelper);
			} else if (item.oneof() != null) { // process one ofs
				final OneOfField oneOfField = new OneOfField(item.oneof(),javaRecordName, lookupHelper);
				int minIndex = oneOfField.fields().get(0).fieldNumber();
				int maxIndex = oneOfField.fields().get(oneOfField.fields().size()-1).fieldNumber();
				final Map<Integer,EnumValue> enumValues = new HashMap<>();
				for(final Field field: oneOfField.fields()) {
					final String fieldType = field.protobufFieldType();
					enumValues.put(field.fieldNumber(), new EnumValue(field.name(),field.depricated(),field.comment()));
				}
				final String enumName = oneOfField.nameCamelFirstUpper()+"OneOfType";
				final String enumComment = """
									/**
									 * Enum for the type of "%s" oneof value
									 */""".formatted(oneOfField.name());
				final String enumString = createEnum(FIELD_INDENT,enumComment ,"",enumName,maxIndex,enumValues, true);
				oneofEnums.add(enumString);
				fields.add(oneOfField);
				fieldDocs.add(new FieldDoc(oneOfField.nameCamelFirstLower(), "<b>("+minIndex+" to "+maxIndex+")</b> "+ cleanJavaDocComment(oneOfField.comment())));
				imports.add("com.hedera.hashgraph.hapi");
			} else if (item.mapField() != null) { // process map fields
				System.err.println("Encountered a mapField that was not handled in "+javaRecordName);
			} else if (item.reserved() != null) { // process reserved
				// reserved are not needed
			} else if (item.field() != null && item.field().fieldName() != null) {
				final SingleField field = new SingleField(item.field(), lookupHelper);
				fields.add(field);
				field.addAllNeededImports(imports, true, false, false, false);
				// build java doc
				if (field.comment() != null) {
					final String fieldJavaDoc = item.field().docComment().getText();
					final String fieldNumComment = "<b>("+field.fieldNumber()+")</b> ";
					fieldDocs.add(new FieldDoc(field.nameCamelFirstLower(), fieldNumComment + cleanJavaDocComment(field.comment())));
				}
			} else if (item.optionStatement() != null){
				if ("deprecated".equals(item.optionStatement().optionName().getText())) {
					deprectaed = "@Deprecated ";
				} else {
					System.err.println("Unhandled Option: "+item.optionStatement().getText());
				}
			} else {
				System.err.println("Unknown Element: "+item+" -- "+item.getText());
			}
		}
		// process field java doc and insert into record java doc
		if (!fieldDocs.isEmpty()) {
			String recordJavaDoc = javaDocComment.length() > 0 ?
					javaDocComment.replaceAll("\n\s*\\*/","") :
					"/**\n * "+javaRecordName;
			recordJavaDoc += "\n *";
			for(var fieldDoc: fieldDocs) {
				recordJavaDoc += "\n * @param "+fieldDoc.fieldName+" "+
						fieldDoc.fieldComment.replaceAll("\n", "\n *         "+" ".repeat(fieldDoc.fieldName.length()));
			}
			recordJavaDoc += "\n */";
			javaDocComment = recordJavaDoc;
		}
		String bodyContent = "";
		if (fields.stream().anyMatch(f -> f instanceof OneOfField || f.optional())) {
			bodyContent += """
					public %s {
					%s
					}
					
					""".formatted(javaRecordName,
					fields.stream()
							.filter(f -> f instanceof OneOfField || f.optional())
							.map(ModelGenerator::generateCostructorCode)
							.collect(Collectors.joining("\n"))
					).replaceAll("\n","\n"+FIELD_INDENT);
		}

		bodyContent += oneofEnums.stream().collect(Collectors.joining("\n    "));

		try (FileWriter javaWriter = new FileWriter(javaFile.toFile())) {
			javaWriter.write("""
					package %s;
					%s
					%s
					%spublic record %s(
					    %s
					){
						%s
					}
					""".formatted(
					javaPackage,
					imports.isEmpty() ? "" : imports.stream().collect(Collectors.joining(".*;\nimport ","\nimport ",".*;\n")),
					javaDocComment,
					deprectaed,
					javaRecordName,
					fields.stream().map(field ->
						FIELD_INDENT+field.javaFieldType() + " " + field.nameCamelFirstLower()
					).collect(Collectors.joining(",\n    ")),
					bodyContent
			));
		}
	}

	private static String generateCostructorCode(final Field f) {
		StringBuilder sb = new StringBuilder(FIELD_INDENT+"""
									if (%s == null) {
										throw new NullPointerException("Parameter '%s' must be supplied and can not be null");
									}""".formatted(f.nameCamelFirstLower(),f.nameCamelFirstLower()));
		if (f instanceof OneOfField) {
			final OneOfField oof = (OneOfField)f;
			for (Field subField: oof.fields()) {
				if(subField.optional()) {
					sb.append("""
       
							// handle special case where protobuf does not have destination between a OneOf with optional 
							// value of empty vs a unset OneOf.
							if(%s.kind() == %sOneOfType.%s && ((Optional)%s.value()).isEmpty()) {
								%s = new OneOf<>(%sOneOfType.UNSET, null);
							}""".formatted(
							f.nameCamelFirstLower(),
							f.nameCamelFirstUpper(),
							camelToUpperSnake(subField.name()),
							f.nameCamelFirstLower(),
							f.nameCamelFirstLower(),
							f.nameCamelFirstUpper()
					));
				}
			}
		}
		return sb.toString().replaceAll("\n","\n"+FIELD_INDENT);
	}
}