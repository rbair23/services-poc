package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hedera.hashgraph.protoparser.Common.capitalizeFirstLetter;

/**
 * Interface for SingleFields and OneOfFields
 */
public interface Field {

	public boolean repeated();
	public FieldType type();
	public int fieldNumber();
	public String name();

	public String protobufFieldType();
	public String computeJavaFieldType();

	public void addAllNeededImports(Set<String> imports);

	public String getParseCode();

	public String javaDefault();

	public String schemaFieldsDef();
	public String parserGetFieldsDefCase();
	public String parserFieldsSetMethodCase();
	public String comment();
	public default String messageType() {
		return null;
	};
	public boolean depricated();
	public default boolean isOptional() {
		return false;
	}

	public enum FieldType {
		MESSAGE("Object", "null"),
		ENUM("int", "null"),
		INT32("int", "0"),
		UINT32("int", "0"),
		SINT32("int", "0"),
		INT64("long", "0"),
		UINT64("long", "0"),
		SINT64("long", "0"),
		FLOAT("long", "0"),
		FIXED32("long", "0"),
		SFIXED32("long", "0"),
		DOUBLE("double", "0"),
		FIXED64("double", "0"),
		SFIXED64("double", "0"),
		STRING("String", "\"\""),
		BOOL("boolean", "false"),
		BYTES("byte[]", "null"),
		ONE_OF("OneOf", "null");

		public final String javaType;
		public final String javaDefault;

		FieldType(String javaType, final String javaDefault) {
			this.javaType = javaType;
			this.javaDefault = javaDefault;
		}

		public String fieldType() {
			String name = toString();
			if (Character.isDigit(name.charAt(name.length()-2))) {
				return name.substring(0,name.length()-2) + "_" + name.substring(name.length()-2);
			} else {
				return name;
			}
		}

		public String javaType(boolean repeated) {
			if (repeated) {
				return switch (javaType) {
					case "int" -> "List<Integer>";
					case "long" -> "List<Long>";
					case "float" -> "List<Float>";
					case "double" -> "List<Double>";
					case "boolean" -> "List<Boolean>";
					default -> "List<" + javaType + ">";
				};
			} else {
				return javaType;
			}
		}

		public static FieldType of(Protobuf3Parser.Type_Context typeContext,  final LookupHelper lookupHelper) {
			if (typeContext.enumType() != null) {
				return FieldType.ENUM;
			} else if (typeContext.messageType() != null) {
				if (lookupHelper.isEnum(typeContext.messageType().getText())) return FieldType.ENUM;
				return FieldType.MESSAGE;
			} else if (typeContext.INT32() != null) {
				return FieldType.INT32;
			} else if (typeContext.UINT32() != null) {
				return FieldType.UINT32;
			} else if (typeContext.SINT32() != null) {
				return FieldType.SINT32;
			} else if (typeContext.INT64() != null) {
				return FieldType.INT64;
			} else if (typeContext.UINT64() != null) {
				return FieldType.UINT64;
			} else if (typeContext.SINT64() != null) {
				return FieldType.SINT64;
			} else if (typeContext.FLOAT() != null) {
				return FieldType.FLOAT;
			} else if (typeContext.FIXED32() != null) {
				return FieldType.FIXED32;
			} else if (typeContext.SFIXED32() != null) {
				return FieldType.SFIXED32;
			} else if (typeContext.DOUBLE() != null) {
				return FieldType.DOUBLE;
			} else if (typeContext.FIXED64() != null) {
				return FieldType.FIXED64;
			} else if (typeContext.SFIXED64() != null) {
				return FieldType.SFIXED64;
			} else if (typeContext.STRING() != null) {
				return FieldType.STRING;
			} else if (typeContext.BOOL() != null) {
				return FieldType.BOOL;
			} else if (typeContext.BYTES() != null) {
				return FieldType.BYTES;
			} else {
				throw new IllegalArgumentException("Unknown field type: "+typeContext);
			}
		}
	}
}
