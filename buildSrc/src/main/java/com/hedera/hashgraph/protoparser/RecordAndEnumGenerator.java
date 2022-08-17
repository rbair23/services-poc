package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Lexer;
import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Code generator that parses protobuf files and generates nice Java source for record files for each message type and
 * enum.
 */
public class RecordAndEnumGenerator {

	/** The base package where all java classes should be placed */
	private static final String DEST_PACKAGE = "com.hedera.hashgraph.hapi.model";
	/** The indent for fields, default 4 spaces */
	private static final String FIELD_INDENT = " ".repeat(4);
	/** Record for a enum value tempory storage */
	private record EnumValue(String name, boolean deprecated, String javaDoc) {}
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
	static void generateRecordsAndEnums(File protoFile, File destinationSrcDir) throws IOException {
		var packageMap = buildMesageToPackageMap(protoFile);
		generate(protoFile, destinationSrcDir, packageMap);
	}

	/**
	 * Builds a map from protobuf message to java package. This is used to produce imports for messages in other packages.
	 *
	 * @param protosFileOrDir a directory containing protobuf files or the protobuf file to parse and look for messages types in
	 * @return map of message name to java package name
	 */
	private static Map<String,String> buildMesageToPackageMap(File protosFileOrDir) {
		Map<String,String> packageMap = new HashMap<>();
		buildMesageToPackageMap(protosFileOrDir,packageMap);
		return packageMap;
	}

	/**
	 * Builds onto map from protobuf message to java package. This is used to produce imports for messages in other packages.
	 *
	 * @param protosFileOrDir a directory containing protobuf files or the protobuf file to parse and look for messages types in
	 * @param packageMap map of message name to java package name to add to
	 */
	private static void buildMesageToPackageMap(File protosFileOrDir, Map<String,String> packageMap) {
		if (protosFileOrDir.isDirectory()) {
			for (final File file : protosFileOrDir.listFiles()) {
				buildMesageToPackageMap(file,packageMap);
			}
		} else if (protosFileOrDir.getName().endsWith(".proto")){
			final String dirName = protosFileOrDir.getParentFile().getName().toLowerCase();
			try (var input = new FileInputStream(protosFileOrDir)) {
				final var lexer = new Protobuf3Lexer(CharStreams.fromStream(input));
				final var parser = new Protobuf3Parser(new CommonTokenStream(lexer));
				var parsedDoc = parser.proto();
//				final String javaPackage = getJavaPackage(parsedDoc); // REMOVED because we want custom packages
				final String javaPackage = computeJavaPackage(dirName);
				for (var topLevelDef : parsedDoc.topLevelDef()) {
					final var msgDef = topLevelDef.messageDef();
					if (msgDef != null) {
						var msgName = msgDef.messageName().getText();
						packageMap.put(msgName, javaPackage);
					}
					final Protobuf3Parser.EnumDefContext enumDef = topLevelDef.enumDef();
					if (enumDef != null) {
						final var enumName = enumDef.enumName().getText();
						packageMap.put(enumName, javaPackage);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
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
	private static void generate(File protoDirOrFile, File destinationSrcDir, Map<String,String> packageMap) throws IOException {
		if (protoDirOrFile.isDirectory()) {
			for (final File file : protoDirOrFile.listFiles()) {
//				if (file.isDirectory() || file.getName().equals("timestamp.proto") || file.getName().equals("basic_types.proto")) {
				if (file.isDirectory() || file.getName().endsWith(".proto")) {
					generate(file, destinationSrcDir, packageMap);
				}
			}
		} else {
			final String dirName = protoDirOrFile.getParentFile().getName().toLowerCase();
			try (var input = new FileInputStream(protoDirOrFile)) {
				final var lexer = new Protobuf3Lexer(CharStreams.fromStream(input));
				final var parser = new Protobuf3Parser(new CommonTokenStream(lexer));
				final Protobuf3Parser.ProtoContext parsedDoc = parser.proto();
//				final String javaPackage = getJavaPackage(parsedDoc); // REMOVED because we want custom packages
				final String javaPackage = computeJavaPackage(dirName);
				final Path packageDir = destinationSrcDir.toPath().resolve(javaPackage.replace('.', '/'));
				Files.createDirectories(packageDir);
				for (var topLevelDef : parsedDoc.topLevelDef()) {
					final Protobuf3Parser.MessageDefContext msgDef = topLevelDef.messageDef();
					if (msgDef != null) {
						generateRecordFile(msgDef, javaPackage, packageDir, packageMap);
					}
					final Protobuf3Parser.EnumDefContext enumDef = topLevelDef.enumDef();
					if (enumDef != null) {
						final var enumName = enumDef.enumName().getText();
						final var javaFile = packageDir.resolve(enumName + ".java");
						generateEnumFile(enumDef, javaPackage,enumName, javaFile.toFile(), packageMap);
					}
				}
			}
		}
	}

	/**
	 * Generate a Java enum from protobuf enum
	 *
	 * @param enumDef the parsed enum def
	 * @param javaPackage the package the enum will be placed in
	 * @param enumName the name of the enum to generate
	 * @param javaFile the java file to write enum into
	 * @param packageMap map of message type to Java package for imports
	 * @throws IOException if there was a problem writing generated code
	 */
	private static void generateEnumFile(Protobuf3Parser.EnumDefContext enumDef, String javaPackage, String enumName, File javaFile, Map<String,String> packageMap) throws IOException {
		String javaDocComment = (enumDef.docComment()== null) ? "" :
				enumDef.docComment().getText()
						.replaceAll("\n \\*\s*\n","\n * <p>\n");
		String deprectaed = "";
		final Map<Integer,EnumValue> enumValues = new HashMap<>();
		int maxIndex = 0;
		for(var item: enumDef.enumBody().enumElement()) {
			if (item.enumField() != null && item.enumField().ident() != null) {
				final var enumValueName = item.enumField().ident().getText();
				final var enumNumber = Integer.parseInt(item.enumField().intLit().getText());
				boolean deprecated = false;
				if (item.enumField().enumValueOptions() != null) {
					for (var option : item.enumField().enumValueOptions().enumValueOption()) {
						if ("deprecated".equals(option.optionName().getText())) {
							deprecated = true;
						} else {
							System.err.println("Unhandled Option on emum: "+item.optionStatement().getText());
						}
					}
				}
				final String enumValueJavaDoc = item.enumField().docComment() == null ? "" :
						item.enumField().docComment().getText();
				maxIndex = Math.max(maxIndex, enumNumber);
				enumValues.put(enumNumber, new EnumValue(enumValueName, false,enumValueJavaDoc));
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
		try (FileWriter javaWriter = new FileWriter(javaFile)) {
			javaWriter.write(
				"package "+javaPackage+";\n"+
					createEnum("", javaDocComment, deprectaed, enumName, maxIndex, enumValues)
			);
		}
	}

	/**
	 * Generate code for a enum
	 *
	 * @param indent extra indent spaces beyond the default 4
	 * @param javaDocComment either enum javadoc comment or empty string
	 * @param deprectaed either @Depricated string or empty string
	 * @param enumName the name for enum
	 * @param maxIndex the max ordinal for enum
	 * @param enumValues map of ordinal to enum value
	 * @return string code for enum
	 */
	private static String createEnum(String indent, String javaDocComment, String deprectaed, String enumName,
			int maxIndex, Map<Integer,EnumValue> enumValues) {

		final List<String> enumValuesCode = new ArrayList<>(maxIndex);
		for (int i = 0; i <= maxIndex; i++) {
			final EnumValue enumValue = enumValues.get(i);
			if (enumValue != null) {
				final String cleanedEnumComment = enumValue.javaDoc
								.replaceAll("[\t\s]*/\\*\\*",FIELD_INDENT+"/**") // clean up doc start indenting
								.replaceAll("\n[\t\s]+\\*","\n"+FIELD_INDENT+" *") // clean up doc indenting
								.replaceAll("/\\*\\*","/**\n"+FIELD_INDENT+" * <b>("+i+")</b>") // add field index
								+ "\n";
				final String deprecatedText = enumValue.deprecated ? FIELD_INDENT+"@Deprecated\n" : "";
				enumValuesCode.add(cleanedEnumComment+deprecatedText+FIELD_INDENT+snakeToCamel(enumValue.name, true)+"("+i+")");
			}
		}
		return """
					%s
					%spublic enum %s {
					%s;
						
						/** The oneof field ordinal in protobuf for this type */
						private final int protobufOrdinal;
						
						/**
						 * OneOf Type Enum Constructor
						 * 
						 * @param protobufOrdinal The oneof field ordinal in protobuf for this type
						 */
						%s(final int protobufOrdinal) {
							this.protobufOrdinal = protobufOrdinal;
						}
						
						/**
						 * Get the oneof field ordinal in protobuf for this type
						 *
						 * @return The oneof field ordinal in protobuf for this type 
						 */
						public int protobufOrdinal() {
							return protobufOrdinal;
						}
						
						/**
						 * Get enum from protobuf ordinal
						 * 
						 * @param ordinal the protobuf ordinal number
						 * @return enum for matching ordinal
						 * @throws IllegalArgumentException if ordinal doesn't exist
						 */
						public static %s fromProtobufOrdinal(int ordinal) {
							return switch(ordinal) {
					%s
								default -> throw new IllegalArgumentException("Unknown protobuf ordinal "+ordinal);
							};
						}
					}
					""".formatted(
							javaDocComment,
							deprectaed,
							enumName,
							enumValuesCode.stream().collect(Collectors.joining(",\n\n")),
							enumName,
							enumName,
							enumValues.entrySet().stream().map((entry) -> {
								return "			case "+entry.getKey()+" -> "+snakeToCamel(entry.getValue().name, true)+";";
							}).collect(Collectors.joining("\n"))
					)
					.replaceAll("\n","\n"+indent);
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
	private static void generateRecordFile(Protobuf3Parser.MessageDefContext msgDef, String javaPackage, Path packageDir, Map<String,String> packageMap) throws IOException {
		final var javaRecordName = msgDef.messageName().getText();
		final var javaFile = packageDir.resolve(javaRecordName + ".java");
		String javaDocComment = (msgDef.docComment()== null) ? "" :
				msgDef.docComment().getText()
						.replaceAll("\n \\*\s*\n","\n * <p>\n");
		String deprectaed = "";
		final List<String> fields = new ArrayList<>();
		final List<String> oneofEnums = new ArrayList<>();
		final List<FieldDoc> fieldDocs = new ArrayList<>();
		final Set<String> imports = new TreeSet<>();
		for(var item: msgDef.messageBody().messageElement()) {
			if (item.messageDef() != null) { // process sub messages
				generateRecordFile(item.messageDef(), javaPackage,packageDir,packageMap);
			} else if (item.oneof() != null) { // process one ofs
				final var oneOfContext = item.oneof();
				final var oneOfComment = oneOfContext.docComment().getText();
				final String oneOfName = oneOfContext.oneofName().getText();
				final Map<Integer,EnumValue> enumValues = new HashMap<>();
				int minIndex = Integer.MAX_VALUE;
				int maxIndex = 0;
				for(var field: oneOfContext.oneofField()) {
					final var fieldComment = field.docComment() == null ? "" : field.docComment().getText();
					final var fieldName = field.fieldName().getText();
					final var fieldType = field.type_().getText();
					final var fieldNumber = Integer.parseInt(field.fieldNumber().intLit().getText());
					boolean deprecated = false;
					if (field.fieldOptions() != null) {
						for (var option : field.fieldOptions().fieldOption()) {
							if ("deprecated".equals(option.optionName().getText())) {
								deprecated = true;
							} else {
								System.err.println("Unhandled Option on emum: "+item.optionStatement().getText());
							}
						}
					}
					final String enumValueName = Character.isLowerCase(fieldType.charAt(0)) || enumValues.values().stream().anyMatch(ev -> ev.name.equals(fieldType)) ? capitalizeFirstLetter(fieldName) : fieldType;
					minIndex = Math.min(minIndex,fieldNumber);
					maxIndex = Math.max(maxIndex,fieldNumber);
					enumValues.put(fieldNumber, new EnumValue(enumValueName,deprecated,fieldComment));
				}
				final String enumName = capitalizeFirstLetter(oneOfName)+"OneOfType";
				final String enumComment = """
									/**
									 * Enum for the type of "%s" oneof value 
									 */""".formatted(oneOfName);
				final String enumString = createEnum(FIELD_INDENT,enumComment ,"",enumName,maxIndex,enumValues);
				oneofEnums.add(enumString);
				fields.add(FIELD_INDENT+"OneOf<"+enumName+"> "+snakeToCamel(oneOfName, false));
				fieldDocs.add(new FieldDoc(snakeToCamel(oneOfName, false), "<b>("+minIndex+" to "+maxIndex+")</b> "+ cleanJavaDocComment(oneOfComment)));
				imports.add("com.hedera.hashgraph.hapi");
			} else if (item.mapField() != null) { // process map fields
				System.err.println("Encountered a mapField that was not handled in "+javaRecordName);
			} else if (item.reserved() != null) { // process reserved
				// reserved are not needed
			} else if (item.field() != null && item.field().fieldName() != null) {
				final var fieldName = item.field().fieldName().getText();
				final Protobuf3Parser.Type_Context typeContext = item.field().type_();
				var fieldType = "Object";
				if (typeContext.messageType() != null) {
					switch (typeContext.messageType().messageName().getText()) {
						case "StringValue":
							fieldType = "Optional<String>";
							imports.add("java.util");
							break;
						case "Int32Value":
						case "UInt32Value":
						case "SInt32Value":
							fieldType = "Optional<Integer>";
							imports.add("java.util");
							break;
						case "BoolValue":
							fieldType = "Optional<Boolean>";
							imports.add("java.util");
							break;
						case "BytesValue":
							fieldType = "Optional<byte[]>";
							imports.add("java.util");
							break;
						default:
							fieldType = typeContext.messageType().messageName().getText();
							final String importPackage = packageMap.get(fieldType);
							if (importPackage != null && !javaPackage.equals(importPackage)) {
								imports.add(packageMap.get(fieldType));
							}
					}
				} else if (typeContext.INT32() != null || typeContext.UINT32() != null || typeContext.SINT32() != null) {
					fieldType = "int";
				} else if (typeContext.INT64() != null || typeContext.UINT64() != null || typeContext.SINT64() != null) {
					fieldType = "long";
				} else if (typeContext.FLOAT() != null || typeContext.FIXED32() != null || typeContext.SFIXED32() != null) {
					fieldType = "float";
				} else if (typeContext.DOUBLE() != null || typeContext.FIXED64() != null || typeContext.SFIXED64() != null) {
					fieldType = "double";
				} else if (typeContext.STRING() != null) {
					fieldType = "String";
				} else if (typeContext.BOOL() != null) {
					fieldType = "boolean";
				} else if (typeContext.BYTES() != null) {
					fieldType = "byte[]";
				}
				final var fieldNumber = Integer.parseInt(item.field().fieldNumber().getText());
				fields.add(FIELD_INDENT+fieldType + " " + snakeToCamel(fieldName, false));
				// build java doc
				if (item.field().docComment() != null) {
					final String fieldJavaDoc = item.field().docComment().getText();
					final String fieldNumComment = "<b>("+fieldNumber+")</b> ";
					fieldDocs.add(new FieldDoc(snakeToCamel(fieldName, false), fieldNumComment + cleanJavaDocComment(fieldJavaDoc)));
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
					fields.stream().collect(Collectors.joining(",\n    ")),
					oneofEnums.stream().collect(Collectors.joining("\n    "))
			));
		}
	}

	/**
	 * Clean up a java doc style comment removing all the "*" etc.
	 *
	 * @param fieldComment raw Java doc style comment
	 * @return clean multi-line content of the comment
	 */
	private static String cleanJavaDocComment(String fieldComment) {
		return fieldComment
				.replaceAll("/\\*\\*[\n\r\s\t]*\\*[\t\s]*|[\n\r\s\t]*\\*/","") // remove java doc
				.replaceAll("\n\s+\\*\s+","\n"); // remove indenting and *
	}

	/**
	 * Extract Java package option from parsed protobuf document
	 *
	 * @param parsedDoc parseed protobuf source
	 * @return the java package option if set or empty string
	 */
	private static String getJavaPackage(Protobuf3Parser.ProtoContext parsedDoc) {
		String packageName = "";
		for(var option: parsedDoc.optionStatement()){
			if ("java_package".equals(option.optionName().getText())) {
				packageName = option.constant().getText().replace("\"","");
			}
		}
		return packageName;
	}


	/**
	 * Compute a destination Java package based on parent directory of the protobuf file
	 *
	 * @param dirName The name of the parent protobuf directory
	 * @return complete java package
	 */
	@NotNull
	private static String computeJavaPackage(final String dirName) {
		return DEST_PACKAGE + (dirName.equals("services") ? "" : "." + dirName);
	}

	/**
	 * Make sure first charachter of a string is upper case
	 *
	 * @param name string input who's first charachter can be upper or lower case
	 * @return name with first charachter converted to upper case
	 */
	private static String capitalizeFirstLetter(String name) {
		if (name.length() > 0) {
			if (name.chars().allMatch(Character::isUpperCase)) {
				return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
			} else {
				return Character.toUpperCase(name.charAt(0)) + name.substring(1);
			}
		}
		return name;
	}

	/**
	 * Convert names like "hello_world" to "HelloWorld" or "helloWorld" depening on firstUpper. Also handles special case
	 * like "HELLO_WORLD" to same output as "hello_world, while "HelloWorld_Two" still becomes "helloWorldTwo".
	 *
	 * @param name input name in snake case
	 * @param firstUpper if true then first char is upper case otherwise it is lower
	 * @return out name in camel case
	 */
	private static String snakeToCamel(String name, boolean firstUpper) {
		final String out =  Arrays.stream(name.split("_")).map(RecordAndEnumGenerator::capitalizeFirstLetter).collect(Collectors.joining(""));
		return firstUpper ? out : Character.toLowerCase(out.charAt(0)) + out.substring(1);
	}
}
