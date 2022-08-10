package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.base.model.SignedTransaction;
import com.hedera.hashgraph.base.model.Transaction;
import io.grpc.MethodDescriptor;

import java.io.InputStream;

public class SignedTransactionMarshaller implements MethodDescriptor.Marshaller<SignedTransaction> {
	@Override
	public InputStream stream(final SignedTransaction value) {
		return null;
	}

	@Override
	public SignedTransaction parse(final InputStream stream) {
		// Read the freakin' bytes and feed them to the parser to handle. Oof. Parser should be stream based???
		return null;
	}
}
