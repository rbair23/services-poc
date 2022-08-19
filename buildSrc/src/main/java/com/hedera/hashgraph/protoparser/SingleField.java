package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;

import java.util.Set;

import static com.hedera.hashgraph.protoparser.Common.camelToUpperSnake;
import static com.hedera.hashgraph.protoparser.Common.isMessageTypeSpecialProtobufValueType;
import static com.hedera.hashgraph.protoparser.Common.snakeToCamel;

/**
 * Record for Field in Protobuf file. Contains all logic and special cases for fields
 *
 * @param repeated
 * @param type
 * @param fieldNumber
 * @param name
 * @param messageType
 */
public record SingleField(boolean repeated, FieldType type, int fieldNumber, String name, String messageType,
						  String messageTypeModelPackage, String messageTypeParserPackage, String comment,
						  boolean depricated, OneOfField parent) implements Field {
	public SingleField(Protobuf3Parser.FieldContext fieldContext, final LookupHelper lookupHelper) {
		this(fieldContext.REPEATED() != null,
				FieldType.of(fieldContext.type_(), lookupHelper),
				Integer.parseInt(fieldContext.fieldNumber().getText()), fieldContext.fieldName().getText(),
				(fieldContext.type_().messageType() == null) ? null :
						fieldContext.type_().messageType().messageName().getText(),
				(fieldContext.type_().messageType() == null || fieldContext.type_().messageType().messageName().getText() == null) ? null :
						lookupHelper.getModelPackage(fieldContext.type_().messageType().messageName().getText()),
				(fieldContext.type_().messageType() == null || fieldContext.type_().messageType().messageName().getText() == null) ? null :
						lookupHelper.getParserPackage(fieldContext.type_().messageType().messageName().getText()),
				fieldContext.docComment() == null ? "" : fieldContext.docComment().getText(),
				getDepricatedOption(fieldContext.fieldOptions()),
				null
		);
		System.out.println("name="+name+"  messageType="+messageType+" typeContext.enumType()="+fieldContext.type_().enumType()+"   FieldType.of(fieldContext.type_())="+FieldType.of(fieldContext.type_(), lookupHelper));
	}

	public SingleField(Protobuf3Parser.OneofFieldContext fieldContext, final OneOfField parent,  final LookupHelper lookupHelper) {
		this(false,
				FieldType.of(fieldContext.type_(), lookupHelper),
				Integer.parseInt(fieldContext.fieldNumber().getText()), fieldContext.fieldName().getText(),
				(fieldContext.type_().messageType() == null) ? null :
						fieldContext.type_().messageType().messageName().getText(),
				(fieldContext.type_().messageType() == null) ? null :
						lookupHelper.getModelPackage(fieldContext.type_().messageType().messageName().getText()),
				(fieldContext.type_().messageType() == null) ? null :
						lookupHelper.getParserPackage(fieldContext.type_().messageType().messageName().getText()),
				fieldContext.docComment() == null ? "" : fieldContext.docComment().getText(),
				getDepricatedOption(fieldContext.fieldOptions()),
				parent
		);
	}

	private static boolean getDepricatedOption(Protobuf3Parser.FieldOptionsContext optionContext) {
		boolean deprecated = false;
		if (optionContext != null) {
			for (var option : optionContext.fieldOption()) {
				if ("deprecated".equals(option.optionName().getText())) {
					deprecated = true;
				} else {
					System.err.println("Unhandled Option on emum: "+optionContext.getText());
				}
			}
		}
		return deprecated;
	}

	@Override
	public boolean isOptional() { // Move logic for checking built in types to common
		return type == SingleField.FieldType.MESSAGE && (
				messageType.equals("StringValue") ||
				messageType.equals("Int32Value") ||
				messageType.equals("SInt32Value") ||
				messageType.equals("BoolValue") ||
				messageType.equals("BytesValue")
		);
	}

	@Override
	public String protobufFieldType() {
		return type == SingleField.FieldType.MESSAGE ? messageType : type.javaType;
	}

	public String computeJavaFieldType() {
		String fieldType = switch(type) {
			case MESSAGE -> messageType;
			case ENUM -> snakeToCamel(messageType, true);
			default -> type.javaType;
		};
		fieldType = switch (fieldType) {
			case "StringValue" -> "Optional<String>";
			case "Int32Value" -> "Optional<Integer>";
			case "UInt32Value" -> "Optional<Integer>";
			case "SInt32Value" -> "Optional<Integer>";
			case "BoolValue" -> "Optional<Boolean>";
			case "BytesValue" -> "Optional<byte[]>";
			default -> fieldType;
		};
		if (repeated) {
			fieldType = switch (fieldType) {
				case "int" -> "List<Integer>";
				case "long" -> "List<Long>";
				case "float" -> "List<Float>";
				case "double" -> "List<Double>";
				case "boolean" -> "List<Boolean>";
				default -> "List<" + fieldType + ">";
			};
		}
		return fieldType;
	}

	public void addAllNeededImports(Set<String> imports) {
		if (repeated || isOptional()) imports.add("java.util");
		if (messageTypeModelPackage != null && messageTypeModelPackage.contains("null")) System.err.println("%%%%%%%% messageTypeModelPackage = "+messageTypeModelPackage);
		if (messageTypeParserPackage != null && messageTypeParserPackage.contains("null")) System.err.println("%%%%%%%% messageTypeParserPackage = "+messageTypeParserPackage);
		if (messageTypeModelPackage != null) imports.add(messageTypeModelPackage);
		if (messageTypeParserPackage != null) imports.add(messageTypeParserPackage);
	}

	public String getParseCode() {
		if (repeated && type == FieldType.MESSAGE) {
			return "new %s().parse(input)".formatted(messageType + ParserGenerator.PASER_JAVA_FILE_SUFFIX);
		} else if (type == FieldType.MESSAGE) {
			return "new %s().parse(input)".formatted(messageType + ParserGenerator.PASER_JAVA_FILE_SUFFIX);
		} else {
			return "input";
		}
	}

	public String javaDefault() {
		if (isOptional()) {
			return "Optional.empty()";
		} else if (repeated) {
			return "null";
		} else {
			return type.javaDefault;
		}
	}

	public String schemaFieldsDef() {
		return "    private static final FieldDefinition %s = new FieldDefinition(\"%s\", FieldType.%s, %s, %d);"
				.formatted(camelToUpperSnake(name), name, type.fieldType(), repeated, fieldNumber);
	}

	public String parserGetFieldsDefCase() {
		return "case %d -> %s;".formatted(fieldNumber, camelToUpperSnake(name));
	}

	public String parserFieldsSetMethodCase() {
		final String fieldNameToSet = parent != null ? parent.name() : name;
//		final String valueToSet = parent != null ? "new OneOf<>(input)" : "input";
		if (isMessageTypeSpecialProtobufValueType(messageType)) {
			if (parent != null) { // one of
				return "case %d -> this.%s = new OneOf<>(%d,%s.%sOneOfType.%s,Optional.of(input));"
						.formatted(fieldNumber, fieldNameToSet, fieldNumber,  parent.parentMessageName(), snakeToCamel(parent.name(), true),snakeToCamel(name, true));
			} else {
				return "case %d -> this.%s = Optional.of(input);".formatted(fieldNumber, fieldNameToSet);
			}
		} else if (type == FieldType.MESSAGE) {
			final String parserClassName = messageType + ParserGenerator.PASER_JAVA_FILE_SUFFIX;
			final String valueToSet = parent != null ?
					"new OneOf<>(%d,%s.%sOneOfType.%s,new %s().parse(input))"
							.formatted(fieldNumber,  parent.parentMessageName(), snakeToCamel(parent.name(), true), snakeToCamel(name, true), parserClassName) :
					"new %s().parse(input)".formatted(parserClassName);
			if (repeated) {
				return """
					case %d -> {
									if (this.%s == null) {
										this.%s = new ArrayList<>();
									}
									this.%s.add(%s);
								}"""
						.formatted(fieldNumber, fieldNameToSet, fieldNameToSet, fieldNameToSet, valueToSet);

			} else {
				return "case %d -> this.%s = %s;".formatted(fieldNumber, fieldNameToSet,valueToSet);
			}
		} else if (type == FieldType.ENUM) {
			// TODO oneof ?
			if (repeated) {
				return "case %d -> this.%s = input.stream().map(%s::fromProtobufOrdinal).toList();".formatted(fieldNumber, fieldNameToSet,
						snakeToCamel(messageType, true));
			} else {
				return "case %d -> this.%s = %s.fromProtobufOrdinal(input);".formatted(fieldNumber, fieldNameToSet,
						snakeToCamel(messageType, true));
			}
		} else {
			final String valueToSet = parent != null ?
					"new OneOf<>(%d,%s.%sOneOfType.%s,input)".formatted(fieldNumber,  parent.parentMessageName(), snakeToCamel(parent.name(), true),snakeToCamel(name, true)) :
					"input";
			return "case %d -> this.%s = %s;".formatted(fieldNumber, fieldNameToSet,valueToSet);
		}
	}
}
