package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hedera.hashgraph.protoparser.Common.capitalizeFirstLetter;
import static com.hedera.hashgraph.protoparser.Common.snakeToCamel;

/**
 * A implementation of Field for OneOf fields 
 */
public record OneOfField(
		String parentMessageName,
		String name,
		String comment,
		List<Field> fields,
		boolean repeated,
		boolean depricated
) implements Field {

	public OneOfField(final Protobuf3Parser.OneofContext oneOfContext, final String parentMessageName, final LookupHelper lookupHelper) {
		this(parentMessageName,
			oneOfContext.oneofName().getText(),
			oneOfContext.docComment().getText(),
			new ArrayList<>(oneOfContext.oneofField().size()),
			false,
			getDepricatedOption(oneOfContext.optionStatement())
		);
		for(var field: oneOfContext.oneofField()) {
			fields.add(new SingleField(field, this, lookupHelper));
		}
	}

	private static boolean getDepricatedOption(List<Protobuf3Parser.OptionStatementContext> optionContext) {
		boolean deprecated = false;
		if (optionContext != null) {
			for (var option : optionContext) {
				if ("deprecated".equals(option.optionName().getText())) {
					deprecated = true;
				} else {
					System.err.println("Unhandled Option on oneof: "+option.optionName().getText());
				}
			}
		}
		return deprecated;
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
	public String protobufFieldType() {
		return "oneof";
	}

	@Override
	public String javaFieldType() {
		String commonType = null;
		boolean allSame = true;
		for(var field: fields) {
			if (commonType == null) {
				commonType =  field.javaFieldType();
			} else if (commonType != field.javaFieldType()) {
				allSame = false;
				break;
			}
		}
		commonType = allSame ? commonType : "Object";
		return "OneOf<"+parentMessageName+"."+ nameCamelFirstUpper()+"OneOfType, "+commonType+">";
	}

	@Override
	public void addAllNeededImports(final Set<String> imports, boolean modelImports,boolean parserImports) {
		imports.add("com.hedera.hashgraph.hapi");
		for(var field:fields) {
			field.addAllNeededImports(imports, modelImports, parserImports);
		}
	}

	@Override
	public String parseCode() {
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
}
