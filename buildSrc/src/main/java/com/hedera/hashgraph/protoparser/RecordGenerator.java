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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class RecordGenerator {

	private static final String FIELD_INDENT = "    ";

	static void generateRecords(File protoFile, File destinationDir) throws IOException {
		var packageMap = buildMesageToPackageMap(protoFile);
		generateFile(protoFile,destinationDir, packageMap);
	}

	private static Map<String,String> buildMesageToPackageMap(File protosDir) {
		Map<String,String> packageMap = new HashMap<>();
		buildMesageToPackageMap(protosDir,packageMap);
		return packageMap;
	}
	private static void buildMesageToPackageMap(File protoFile, Map<String,String> packageMap) {
		if (protoFile.isDirectory()) {
			for (final File file : protoFile.listFiles()) {
				buildMesageToPackageMap(file,packageMap);
			}
		} else if (protoFile.getName().endsWith(".proto")){
			final String dirName = protoFile.getParentFile().getName().toLowerCase();
			try (var input = new FileInputStream(protoFile)) {
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

	private static void generateFile(File protoFile, File destinationDir, Map<String,String> packageMap) throws IOException {
		if (protoFile.isDirectory()) {
			for (final File file : protoFile.listFiles()) {
//				if (file.isDirectory() || file.getName().equals("timestamp.proto") || file.getName().equals("basic_types.proto")) {
				if (file.isDirectory() || file.getName().endsWith(".proto")) {
					generateFile(file, destinationDir, packageMap);
				}
			}
		} else {
			final String dirName = protoFile.getParentFile().getName().toLowerCase();
			try (var input = new FileInputStream(protoFile)) {
				final var lexer = new Protobuf3Lexer(CharStreams.fromStream(input));
				final var parser = new Protobuf3Parser(new CommonTokenStream(lexer));
				final Protobuf3Parser.ProtoContext parsedDoc = parser.proto();
//				final String javaPackage = getJavaPackage(parsedDoc); // REMOVED because we want custom packages
				final String javaPackage = computeJavaPackage(dirName);
				final Path packageDir = destinationDir.toPath().resolve(javaPackage.replace('.', '/'));
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

	private record EnumValue(String name, boolean deprecated, String javaDoc) {}

	private static void generateEnumFile(Protobuf3Parser.EnumDefContext enumDef, String javaPackage, String javaRecordName, File javaFile, Map<String,String> packageMap) throws IOException {
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
						item.enumField().docComment().getText()
							.replaceAll("[\t\s]*/\\*\\*",FIELD_INDENT+"/**") // clean up doc start indenting
							.replaceAll("\n[\t\s]+\\*","\n"+FIELD_INDENT+" *") // clean up doc indenting
							.replaceAll("/\\*\\*","/**\n"+FIELD_INDENT+" * <b>("+enumNumber+")</b>") // add field index
						+ "\n";

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
		final List<String> enumValuesCode = new ArrayList<>(maxIndex);
		for (int i = 0; i < maxIndex; i++) {
			final EnumValue enumValue = enumValues.get(i);
			if (enumValue == null) {
				enumValuesCode.add(FIELD_INDENT+"MISSING_"+i);
			} else {
				final String deprecatedText = enumValue.deprecated ? FIELD_INDENT+"@deprecated\n" : "";
				enumValuesCode.add(enumValue.javaDoc+deprecatedText+FIELD_INDENT+enumValue.name);
			}
		}

		try (FileWriter javaWriter = new FileWriter(javaFile)) {
			javaWriter.write("""
					package %s;
					%s
					%spublic enum %s {
					%s
					}
					""".formatted(
					javaPackage,
					javaDocComment,
					deprectaed,
					javaRecordName,
					enumValuesCode.stream().collect(Collectors.joining(",\n\n"))
			));
		}
	}

	private static void generateRecordFile(Protobuf3Parser.MessageDefContext msgDef, String javaPackage, Path packageDir, Map<String,String> packageMap) throws IOException {
		final var javaRecordName = msgDef.messageName().getText();
		final var javaFile = packageDir.resolve(javaRecordName + ".java");
		String javaDocComment = (msgDef.docComment()== null) ? "" :
				msgDef.docComment().getText()
						.replaceAll("\n \\*\s*\n","\n * <p>\n");
		String deprectaed = "";
		final List<String> fields = new ArrayList<>();
		final Map<String,String> fieldDocs = new HashMap<>();
		final Set<String> imports = new TreeSet<>();
		for(var item: msgDef.messageBody().messageElement()) {
			if (item.messageDef() != null) { // process sub messages
				generateRecordFile(item.messageDef(), javaPackage,packageDir,packageMap);
			}
			if (item.field() != null && item.field().fieldName() != null) {
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
				fields.add(FIELD_INDENT+fieldType + " " + fieldName);
				// build java doc
				if (item.field().docComment() != null) {
					final String fieldJavaDoc = item.field().docComment().getText();
					final String rawContent = fieldJavaDoc
							.replaceAll("/\\*\\*[\n\r\s\t]*\\*[\t\s]*|[\n\r\s\t]*\\*/","") // remove java doc
							.replaceAll("\n\s+\\*\s+","\n"); // remove indenting and *
					final String fieldNumComment = "<b>("+fieldNumber+")</b> ";
					fieldDocs.put(fieldName, fieldNumComment + rawContent);
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
			for(var fieldDoc: fieldDocs.entrySet()) {
				recordJavaDoc += "\n * @param "+fieldDoc.getKey()+" "+
						fieldDoc.getValue().replaceAll("\n", "\n *         "+" ".repeat(fieldDoc.getKey().length()));
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
					){}
					""".formatted(
					javaPackage,
					imports.isEmpty() ? "" : imports.stream().collect(Collectors.joining(".*;\nimport ","\nimport ",".*;\n")),
					javaDocComment,
					deprectaed,
					javaRecordName,
					fields.stream().collect(Collectors.joining(",\n    "))
			));
		}
	}

	private static String getJavaPackage(Protobuf3Parser.ProtoContext parsedDoc) {
		String packageName = "";
		for(var option: parsedDoc.optionStatement()){
			if ("java_package".equals(option.optionName().getText())) {
				packageName = option.constant().getText().replace("\"","");
			}
		}
		return packageName;
	}


	@NotNull
	private static String computeJavaPackage(final String dirName) {
		return "com.hedera.hashgraph.hapi.model" + (dirName.equals("services") ? "" : "." + dirName);
	}
}
