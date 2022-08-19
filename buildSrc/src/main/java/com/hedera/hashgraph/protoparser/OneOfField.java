package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hedera.hashgraph.protoparser.Common.capitalizeFirstLetter;
import static com.hedera.hashgraph.protoparser.Common.snakeToCamel;

public class OneOfField implements Field {
	private final String parentMessageName;
	private final String name;
	private final String comment;
	private final List<Field> fields;

	public OneOfField(final Protobuf3Parser.OneofContext oneOfContext, final String parentMessageName, final LookupHelper lookupHelper) {
		this.parentMessageName = parentMessageName;
		this.name = oneOfContext.oneofName().getText();
		this.comment = oneOfContext.docComment().getText();
		this.fields = oneOfContext.oneofField().stream()
				.map(field -> new SingleField(field, this, lookupHelper)).collect(
				Collectors.toList());
	}

	public String parentMessageName() {
		return parentMessageName;
	}

	public List<Field> fields() {
		return fields;
	}

	@Override
	public boolean repeated() {
		return false;
	}

	@Override
	public FieldType type() {
		return FieldType.ONE_OF;
	}

	@Override
	public int fieldNumber() {
		return -1;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String protobufFieldType() {
		return "oneof";
	}

	@Override
	public String computeJavaFieldType() {
		String commonType = null;
		boolean allSame = true;
		for(var field: fields) {
			if (commonType == null) {
				commonType =  field.computeJavaFieldType();
			} else if (commonType != field.computeJavaFieldType()) {
				allSame = false;
				break;
			}
		}
		commonType = allSame ? commonType : "Object";
		return "OneOf<"+parentMessageName+"."+ snakeToCamel(name, true)+"OneOfType, "+commonType+">";
	}

	@Override
	public void addAllNeededImports(final Set<String> imports) {
		imports.add("com.hedera.hashgraph.hapi");
		for(var field:fields) {
			field.addAllNeededImports(imports);
		}
	}

	@Override
	public String getParseCode() {
		return null;
	}

	@Override
	public String javaDefault() {
		return "null";
	}

	public String schemaFieldsDef() {
		return fields.stream().map(field -> field.schemaFieldsDef()).collect(Collectors.joining("\n"));
	}

	public String parserGetFieldsDefCase() {
		return fields.stream().map(field -> field.parserGetFieldsDefCase()).collect(Collectors.joining("\n            "));
	}
	public String parserFieldsSetMethodCase() {
		return fields.stream().map(field -> field.parserFieldsSetMethodCase()).collect(Collectors.joining("\n"));
	}

	public String comment(){
		return comment;
	}

	public boolean depricated() {
		return false; // TODO is there a better answer here
	}

//	public String getEnumClassName() {
//		return "%s.%sOneOfType".formatted(capitalizeFirstLetter(parent.name()), capitalizeFirstLetter(name))
//
//	}
}
