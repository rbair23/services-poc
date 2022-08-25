package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;

import java.util.Set;

import static com.hedera.hashgraph.protoparser.Common.snakeToCamel;

/**
 * Interface for SingleFields and OneOfFields
 */
public interface Field {

	/**
	 * Is this field a repeated field. Repeated fields are lists of values rather than a single value.
	 *
	 * @return true if this field is a list and false if it is a single value
	 */
	public boolean repeated();

	/**
	 * Get the field number, the number of the field in the parent message
	 *
	 * @return this fields number
	 */
	public int fieldNumber();

	/**
	 * Get this fields name in orginal case and format
	 *
	 * @return this fields name
	 */
	public String name();

	/**
	 * Get this fields name converted to camel case with the first leter upper case
	 *
	 * @return this fields name converted
	 */
	public default String nameCamelFirstUpper() {
		return snakeToCamel(name(),true);
	}

	/**
	 * Get this fields name converted to camel case with the first leter lower case
	 *
	 * @return this fields name converted
	 */
	public default String nameCamelFirstLower() {
		return snakeToCamel(name(),false);
	}

	/**
	 * Get the field type for this field, the field type is independent of repeated
	 *
	 * @return this fields type
	 */
	public FieldType type();

	/**
	 * Get the protobuf field type for this field
	 *
	 * @return this fields type in protobuf format
	 */
	public String protobufFieldType();

	/**
	 * Get the Java field type for this field
	 *
	 * @return this fields type in Java format
	 */
	public String javaFieldType();

	public void addAllNeededImports(Set<String> imports, boolean modelImports,boolean parserImports,
			final boolean writerImports);

	public String parseCode();

	public String javaDefault();

	public String schemaFieldsDef();
	public String parserGetFieldsDefCase();
	public String parserFieldsSetMethodCase();
	public String comment();
	public default String messageType() {
		return null;
	};
	public boolean depricated();
	public default boolean optional() {
		return false;
	}
	public default OneOfField parent() {
		return null;
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
