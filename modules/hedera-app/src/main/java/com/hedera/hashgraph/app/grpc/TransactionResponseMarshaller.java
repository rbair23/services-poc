package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.base.model.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.io.InputStream;

public class TransactionResponseMarshaller implements MethodDescriptor.Marshaller<TransactionResponse> {
	@Override
	public InputStream stream(final TransactionResponse value) {
		return null;
	}

	@Override
	public TransactionResponse parse(final InputStream stream) {
		return null;
	}
}
