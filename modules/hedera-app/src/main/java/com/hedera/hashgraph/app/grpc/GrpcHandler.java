package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.base.model.ResponseCode;
import com.hedera.hashgraph.base.model.SignedTransaction;
import com.hedera.hashgraph.base.model.Transaction;
import com.hedera.hashgraph.base.model.TransactionBody;
import com.hedera.hashgraph.base.model.TransactionResponse;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GrpcHandler {

	public static EndpointBuilder endpoint(String serviceName) {
		return new EndpointBuilder(serviceName);
	}

	public static final class EndpointBuilder {
		private List<ServerMethodDefinition<?, ?>> methods = new ArrayList<>();
		private final String serviceName;

		private EndpointBuilder(String serviceName) {
			// TODO Validate input
			this.serviceName = serviceName;
		}

		public EndpointBuilder signedTransaction(String methodName, Function<TransactionBody, ResponseCode> handler) {
			methods.add(ServerMethodDefinition.create(
					MethodDescriptor.<SignedTransaction, TransactionResponse>newBuilder()
							.setFullMethodName(serviceName + "/" + methodName)
							.setSampledToLocalTracing(true)
							.setType(MethodDescriptor.MethodType.UNARY)
							.setRequestMarshaller(new SignedTransactionMarshaller())
							.setResponseMarshaller(new TransactionResponseMarshaller())
							.build(),
					(call, headers) -> new ServerCall.Listener<>() {
						@Override
						public void onMessage(final SignedTransaction tx) {
							// First do the basic validation of the Transaction / SignedTransaction.
							// TODO Some validation on signatures on "tx"
							// Then deserialize the TransactionBody
							// TODO actually parse the bytes to get the TransactionBody
//							final var body = new TransactionBody(null, null, 0, null, null, null);
							// Then validate the TransactionBody
							// TODO do some validation on the transaction body. Does the account exist? Right shard/realm?
							// Then delegate to the service
//							final var response = handler.apply(body);
//							System.out.println(response);
							// TODO Map response to the right code, and add trailers
//							call.close(Status.fromCode(Status.Code.OK), null);
						}
					}
			));
			return this;
		}

		public EndpointBuilder transaction(String methodName, Function<TransactionBody, ResponseCode> handler) {
			methods.add(ServerMethodDefinition.create(
					MethodDescriptor.<Transaction, TransactionResponse>newBuilder()
							.setFullMethodName(serviceName + "/" + methodName)
							.setSampledToLocalTracing(true)
							.setType(MethodDescriptor.MethodType.UNARY)
							.setRequestMarshaller(new TransactionMarshaller())
							.setResponseMarshaller(new TransactionResponseMarshaller())
							.build(),
					(call, headers) -> new ServerCall.Listener<>() {
						@Override
						public void onMessage(final Transaction message) {
							// First do the basic validation of the Transaction / SignedTransaction.
							// Then deserialize the TransactionBody
							// Then validate the TransactionBody
							// Then delegate to the service
							final var response = handler.apply(null);
							System.out.println(response);
							// TODO Map response to the right code, and add trailers
//							call.close(Status.fromCode(Status.Code.OK), null);
						}
					}
			));
			return this;
		}

		public ServerServiceDefinition build() {
			final var builder = ServerServiceDefinition
					.builder(serviceName);

			for (final var method : methods) {
				builder.addMethod(method);
			}

			return builder.build();
		}
	}
}
