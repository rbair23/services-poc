package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.base.model.Transaction;
import io.grpc.MethodDescriptor;

import java.io.InputStream;

public class TransactionMarshaller implements MethodDescriptor.Marshaller<Transaction> {
	@Override
	public InputStream stream(final Transaction value) {
		return null;
	}

	@Override
	public Transaction parse(final InputStream stream) {
		// Read the freakin' bytes and feed them to the parser to handle. Oof. Parser should be stream based???
		return null;
	}
}
