package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Common functions and constants for code generation
 */
public class Common {
	/** The indent for fields, default 4 spaces */

	public static final String FIELD_INDENT = " ".repeat(4);
	/** The base package where all java classes should be placed */
	public static final String PARSERS_DEST_PACKAGE = "com.hedera.hashgraph.hapi.parsers.proto";

	/** The base package where all java classes should be placed */
	public static final String MODELS_DEST_PACKAGE = "com.hedera.hashgraph.hapi.model";

	/**
	 * Compute a destination Java package based on parent directory of the protobuf file
	 *
	 * @param destPackage the base package to start from
	 * @param dirName The name of the parent protobuf directory
	 * @return complete java package
	 */
	@NotNull
	public static String computeJavaPackage(final String destPackage, final String dirName) {
		return destPackage + computeJavaPackageSuffix(dirName);
	}

	/**
	 * Compute a destination Java package suffix based on parent directory of the protobuf file
	 *
	 * @param dirName The name of the parent protobuf directory
	 * @return complete java package
	 */
	@NotNull
	public static String computeJavaPackageSuffix(final String dirName) {
		return (dirName.equals("services") ? "" : "." + dirName);
	}

	/**
	 * Extract Java package option from parsed protobuf document
	 *
	 * @param parsedDoc parseed protobuf source
	 * @return the java package option if set or empty string
	 */
	public static String getJavaPackage(Protobuf3Parser.ProtoContext parsedDoc) {
		String packageName = "";
		for(var option: parsedDoc.optionStatement()){
			if ("java_package".equals(option.optionName().getText())) {
				packageName = option.constant().getText().replace("\"","");
			}
		}
		return packageName;
	}

	/**
	 * Make sure first charachter of a string is upper case
	 *
	 * @param name string input who's first charachter can be upper or lower case
	 * @return name with first charachter converted to upper case
	 */
	public static String capitalizeFirstLetter(String name) {
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
	public static String snakeToCamel(String name, boolean firstUpper) {
		final String out =  Arrays.stream(name.split("_")).map(Common::capitalizeFirstLetter).collect(
				Collectors.joining(""));
		return firstUpper ? out : Character.toLowerCase(out.charAt(0)) + out.substring(1);
	}

	/**
	 * Convert a camel case name to upper case snake case
	 *
	 * @param name the input name in camel case
	 * @return output name in upper snake case
	 */
	public static String camelToUpperSnake(String name) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);
			if (Character.isUpperCase(c) && i > 0) {
				buf.append("_");
				buf.append(c);
			} else {
				buf.append(Character.toUpperCase(c));
			}
		}
		return buf.toString();
	}

	/**
	 * Clean up a java doc style comment removing all the "*" etc.
	 *
	 * @param fieldComment raw Java doc style comment
	 * @return clean multi-line content of the comment
	 */
	public static String cleanJavaDocComment(String fieldComment) {
		return fieldComment
				.replaceAll("/\\*\\*[\n\r\s\t]*\\*[\t\s]*|[\n\r\s\t]*\\*/","") // remove java doc
				.replaceAll("\n\s+\\*\s+","\n"); // remove indenting and *
	}

	/**
	 * Check if a messgae type is one of the protobuf built in primative value types
	 *
	 * @param messageType message type string
	 * @return true if special value type
	 */
	public static boolean isMessageTypeSpecialProtobufValueType(String messageType) {
		if (messageType == null) return false;
		return switch(messageType) {
			case "StringValue" -> true;
			case "Int32Value" -> true;
			case "UInt32Value" -> true;
			case "SInt32Value" -> true;
			case "Int64Value" -> true;
			case "UInt64Value" -> true;
			case "SInt64Value" -> true;
			case "BoolValue" -> true;
			case "BytesValue" -> true;
			default -> false;
		};
	}
}
