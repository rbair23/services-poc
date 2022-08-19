package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.hapi.model.base.Transaction;
import com.hedera.hashgraph.hapi.proto.parsers.TransactionParser;
import com.hedera.hashgraph.protoparse.MalformedProtobufException;
import io.grpc.MethodDescriptor;

import java.io.IOException;
import java.io.InputStream;

public class TransactionMarshaller implements MethodDescriptor.Marshaller<Transaction> {
	private final ThreadLocal<TransactionParser> parserThreadLocal = new ThreadLocal<>() {
		@Override
		protected TransactionParser initialValue() {
			return new TransactionParser();
		}
	};

	@Override
	public InputStream stream(final Transaction value) {
		return null;
	}

	@Override
	public Transaction parse(final InputStream stream) {
		try {
			final var parser = parserThreadLocal.get();
			return parser.parse(stream);
		} catch (IOException | MalformedProtobufException e) {
			// TODO I need to have some runtime exceptions for protobuf parse errors so I can throw that here,
			//      because runtime exceptions are the only way to signal my sadness here.
			throw new RuntimeException(e);
		}
	}
}
