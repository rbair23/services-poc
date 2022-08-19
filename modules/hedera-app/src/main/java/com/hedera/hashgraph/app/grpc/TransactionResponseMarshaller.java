package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.hapi.model.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TransactionResponseMarshaller implements MethodDescriptor.Marshaller<TransactionResponse> {
	@Override
	public InputStream stream(final TransactionResponse value) {
		return new ByteArrayInputStream(new byte[0]);
	}

	@Override
	public TransactionResponse parse(final InputStream stream) {
		return null;
	}
}
