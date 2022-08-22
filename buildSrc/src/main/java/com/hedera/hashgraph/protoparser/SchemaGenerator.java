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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.hedera.hashgraph.protoparser.Common.MODELS_DEST_PACKAGE;
import static com.hedera.hashgraph.protoparser.Common.SCHEMAS_DEST_PACKAGE;
import static com.hedera.hashgraph.protoparser.Common.computeJavaPackage;

/**
 * Code generator that parses protobuf files and generates schemas for each message type.
 */
public class SchemaGenerator {

	/** Suffix for schema java classes */
	public static final String SCHEMA_JAVA_FILE_SUFFIX = "Schema";

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
	static void generateSchemas(File protoDir, File destinationSrcDir, final LookupHelper lookupHelper) throws IOException {
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
				final String javaPackage = computeJavaPackage(SCHEMAS_DEST_PACKAGE, dirName);
				final Path packageDir = destinationSrcDir.toPath().resolve(javaPackage.replace('.', '/'));
				Files.createDirectories(packageDir);
				for (var topLevelDef : parsedDoc.topLevelDef()) {
					final Protobuf3Parser.MessageDefContext msgDef = topLevelDef.messageDef();
					if (msgDef != null) {
						generateRecordFile(msgDef, dirName, javaPackage, packageDir, lookupHelper);
					}
				}
			}
		}
	}

	/**
	 * Generate a Java record from protobuf message type
	 *
	 * @param msgDef the parsed message
	 * @param dirName the directory name of the dir containing the protobuf file
	 * @param javaPackage the java package the record file should be generated in
	 * @param packageDir the output package directory
	 * @param lookupHelper helper for global context
	 * @throws IOException If there was a problem writing record file
	 */
	private static void generateRecordFile(Protobuf3Parser.MessageDefContext msgDef, String dirName, String javaPackage,
			Path packageDir, final LookupHelper lookupHelper) throws IOException {
		final var modelClassName = msgDef.messageName().getText();
		final var parserClassName = modelClassName+ SCHEMA_JAVA_FILE_SUFFIX;
		final var javaFile = packageDir.resolve(parserClassName + ".java");
		String javaDocComment = (msgDef.docComment()== null) ? "" :
				msgDef.docComment().getText()
						.replaceAll("\n \\*\s*\n","\n * <p>\n");
		String deprectaed = "";
		final List<Field> fields = new ArrayList<>();
		final Set<String> imports = new TreeSet<>();
		final String moidelJavaPackage = computeJavaPackage(MODELS_DEST_PACKAGE, dirName);
		for(var item: msgDef.messageBody().messageElement()) {
			if (item.messageDef() != null) { // process sub messages
				generateRecordFile(item.messageDef(), dirName, javaPackage,packageDir,lookupHelper);
			} else if (item.oneof() != null) { // process one ofs
				final var field = new OneOfField(item.oneof(), modelClassName, lookupHelper);
				fields.add(field);
				field.addAllNeededImports(imports, true, false);
			} else if (item.mapField() != null) { // process map fields
				throw new IllegalStateException("Encountered a mapField that was not handled in "+ parserClassName);
			} else if (item.reserved() != null) { // process reserved
				// reserved are not needed
			} else if (item.field() != null && item.field().fieldName() != null) {
				final var field = new SingleField(item.field(), lookupHelper);
				fields.add(field);
			} else if (item.optionStatement() != null){
				// no needed for now
			} else {
				System.err.println("Unknown Element: "+item+" -- "+item.getText());
			}
		}

		final List<Field> flattenedFields = fields.stream()
				.flatMap(field -> field instanceof OneOfField ? ((OneOfField)field).fields().stream() :
						Collections.singleton(field).stream())
				.collect(Collectors.toList());

		try (FileWriter javaWriter = new FileWriter(javaFile.toFile())) {
			javaWriter.write("""
					package %s;
										
					import com.hedera.hashgraph.protoparse.FieldDefinition;
					import com.hedera.hashgraph.protoparse.FieldType;
					import com.hedera.hashgraph.hapi.Schema;
					%s
										
					/**
					 * Schema for %s model object. Generate based on protobuf schema.
					 */
					public class %s implements Schema {
						// -- FIELD DEFINITIONS ---------------------------------------------
						
					%s
										
						// -- OTHER METHODS -------------------------------------------------
						
						/**
						 * Check if a field definition belongs to this schema.
						 *
						 * @param f field def to check
						 * @return true if it belongs to this schema
						 */
						public static boolean valid(FieldDefinition f) {
							return f != null && getField(f.number()) != null;
						}
						
					%s
					}
					""".formatted(
						javaPackage,
						imports.isEmpty() ? "" : imports.stream()
								.filter(input -> !input.equals(javaPackage))
								.collect(Collectors.joining(".*;\nimport ","\nimport ",".*;\n")),
						modelClassName,
						parserClassName,
						fields.stream().map(field -> field.schemaFieldsDef()).collect(Collectors.joining("\n")),
						generateGetField(flattenedFields)
					)
			);
		}
	}

	private static String generateGetField(final List<Field> fields) {
		return 	"""		
					/**
					 * Get a field definition given a field number
					 *
					 * @param fieldNumber the fields number to get def for
					 * @return field def or null if field number does not exist
					 */
					public static FieldDefinition getField(final int fieldNumber) {
						return switch(fieldNumber) {
						    %s
							default -> null;
						};
					}
				""".formatted(fields.stream()
											.map(Field::parserGetFieldsDefCase)
											.collect(Collectors.joining("\n            ")));
	}

}
