package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hedera.hashgraph.protoparser.Common.FIELD_INDENT;
import static com.hedera.hashgraph.protoparser.Common.camelToUpperSnake;
import static com.hedera.hashgraph.protoparser.Common.cleanJavaDocComment;

/**
 * Code for generating enum code
 */
public class EnumGenerator {

	/** Record for a enum value tempory storage */
	record EnumValue(String name, boolean deprecated, String javaDoc) {}

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
	static void generateEnumFile(Protobuf3Parser.EnumDefContext enumDef, String javaPackage, String enumName, File javaFile, final LookupHelper lookupHelper) throws IOException {
		String javaDocComment = (enumDef.docComment()== null) ? "" :
				enumDef.docComment().getText()
						.replaceAll("\n \\*\s*\n","\n * <p>\n");
		String deprectaed = "";
		final Map<Integer, EnumValue> enumValues = new HashMap<>();
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
	static String createEnum(String indent, String javaDocComment, String deprectaed, String enumName,
			int maxIndex, Map<Integer, EnumValue> enumValues) {
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
				enumValuesCode.add(cleanedEnumComment+deprecatedText+FIELD_INDENT+camelToUpperSnake(enumValue.name)+"("+i+")");
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
							return "			case "+entry.getKey()+" -> "+camelToUpperSnake(entry.getValue().name)+";";
						}).collect(Collectors.joining("\n"))
				)
				.replaceAll("\n","\n"+indent);
	}
}
