package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.app.workflows.ingest.IngestContext;
import com.hedera.hashgraph.app.workflows.ingest.TransactionIngestWorkflow;
import com.hedera.hashgraph.hapi.model.TransactionResponse;
import com.hedera.hashgraph.hapi.model.base.Transaction;
import com.hedera.hashgraph.hapi.proto.parsers.TransactionParser;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ServerCalls;
import io.helidon.grpc.core.MarshallerSupplier;
import io.helidon.grpc.server.ServiceDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GrpcHandler {
	private static final HederaMarshallerSupplier MARSHALLER_SUPPLIER = new HederaMarshallerSupplier();

	private final ThreadLocal<IngestContext> contextThreadLocal = new ThreadLocal<>() {
		@Override
		protected IngestContext initialValue() {
			// Oh crap, or do I take a factory? Hopefully this context can create its own stuff and not need
			// much from the outside world...
			return new IngestContext();
		}
	};

	private final TransactionIngestWorkflow ingestWorkflow;

	public GrpcHandler(TransactionIngestWorkflow ingestWorkflow) {
		this.ingestWorkflow = Objects.requireNonNull(ingestWorkflow);
	}

	public ServiceBuilder service(String serviceName) {
		return new ServiceBuilder(serviceName);
	}

	public final class ServiceBuilder {
		private final String serviceName;
		private final Map<String, ServerCalls.UnaryMethod<Transaction, TransactionResponse>> methods = new HashMap<>();

		private ServiceBuilder(String serviceName) {
			// TODO Validate input
			this.serviceName = serviceName;
		}

		public ServiceBuilder transaction(String methodName) {
			methods.put(methodName, (tx, responseObserver) -> {
				try {
					ingestWorkflow.run(contextThreadLocal.get(), tx);
					// TODO Create a transaction response. From workflow? Not sure. Then return it below.
					responseObserver.onNext(null); // Return this to the client
					responseObserver.onCompleted(); // Shut it down, fool.
				} catch (Throwable th) {
					// TODO Log this. Something awful happened? Maybe normal? Not sure.
					responseObserver.onError(th);
				}
			});
			return this;
		}

		public ServiceDescriptor build() {
			final var builder = ServiceDescriptor.builder(null, serviceName);
			methods.forEach((k, v) -> builder.unary(k, v, rules -> rules.marshallerSupplier(MARSHALLER_SUPPLIER)));
			return builder.build();
		}
	}

	private static final class HederaMarshallerSupplier implements MarshallerSupplier {
		private final Map<Class<?>, MethodDescriptor.Marshaller<?>> marshallerMap =
				new HashMap<>();

		HederaMarshallerSupplier() {
			marshallerMap.put(Transaction.class, new TransactionMarshaller());
			marshallerMap.put(TransactionResponse.class, new TransactionResponseMarshaller());
			// TODO Add support for queries here
		}

		@Override
		public <T> MethodDescriptor.Marshaller<T> get(Class<T> clazz) {
			// TODO Note that this isn't safe across classloaders -- i.e. if a clazz is passed in from another
			//      classloader, then boom. Probably not a problem, but should be documented somewhere...
			//noinspection unchecked
			return (MethodDescriptor.Marshaller<T>) marshallerMap.get(clazz);
		}
	}
}
