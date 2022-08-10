package com.hedera.hashgraph.file.impl.serde;

import com.hedera.hashgraph.file.model.FileCreateTransactionBody;
import com.hedera.hashgraph.protoparse.FieldDefinition;
import com.hedera.hashgraph.protoparse.FieldType;
import com.hedera.hashgraph.protoparse.ProtoParser;

// NOTE: Not exported from the "file" module!
// QUESTION? Should this class be used both for protobuf parsing and for JSON parsing? Protobuf itself
// claims that it can do both, and we want to be able to do both. Maybe in the future we want to be able
// to do even more kinds of serialization and deserialization if we decide to support additional formats
// (which at this point seems unlikely -- JSON and Protobuf are probably going to be it for a long time).
// Fundamentally, both should be generated...
public final class FileCreateTransactionBodyParser extends ProtoParser {
	private static final FieldDefinition CONTENTS = new FieldDefinition("contents", FieldType.BYTES, false, 4);
	private static final FieldDefinition MEMO = new FieldDefinition("memo", FieldType.STRING, false, 8);

	private byte[] contents;
	private String memo;

	private FileCreateTransactionBodyParser(final byte[] protobuf) {
		super(protobuf);
	}

	public static FileCreateTransactionBody parse(final byte[] protobuf) {
		// TODO implement
		return new FileCreateTransactionBody(new byte[0], "Some memo!");
	}

	@Override
	protected FieldDefinition getFieldDefinition(final int i) {
		return switch(i) {
			case 4 -> CONTENTS;
			case 8 -> MEMO;
			default -> null;
		};
	}

	@Override
	public void stringField(final int fieldNum, final String value) {
		this.memo = value;
	}

	@Override
	public void byteField(final int fieldNum, final byte[] value) {
		this.contents = value;
	}
}