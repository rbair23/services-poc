package com.hedera.hashgraph.app.grpc;

import io.grpc.MethodDescriptor;
import io.helidon.grpc.core.MarshallerSupplier;
import io.helidon.grpc.server.ServiceDescriptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ServiceBuilder {
    private static final NoopMarshaller NOOP_MARSHALLER = new NoopMarshaller();

    // Create a single instance of the marshaller supplier to provide to every gRPC method registered
    // with the system. We only need the one, and it always returns the same NoopMarshaller instance.
    /**
     *
     */
    private static final MarshallerSupplier MARSHALLER_SUPPLIER = new MarshallerSupplier() {
        @Override
        public <T> MethodDescriptor.Marshaller<T> get(Class<T> clazz) {
            //noinspection unchecked
            return (MethodDescriptor.Marshaller<T>) NOOP_MARSHALLER;
        }
    };

    private final String serviceName;
    private final Set<String> transactionMethods = new HashSet<>();
    private final TransactionMethod transactionMethod;

    ServiceBuilder(String serviceName, TransactionMethod transactionMethod) {
        this.transactionMethod = Objects.requireNonNull(transactionMethod);
        this.serviceName = Objects.requireNonNull(serviceName);
        if (serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName cannot be blank");
        }
    }

    public ServiceBuilder transaction(String methodName) {
        transactionMethods.add(methodName);
        return this;
    }

    public ServiceDescriptor build() {
        final var builder = ServiceDescriptor.builder(null, serviceName);
        transactionMethods.forEach(methodName -> builder.unary(
                methodName,
                transactionMethod,
                rules -> rules.marshallerSupplier(MARSHALLER_SUPPLIER)));
        return builder.build();
    }

    private static final class NoopMarshaller implements MethodDescriptor.Marshaller<byte[]> {
        private static final int MAX_MESSAGE_SIZE = 1024 * 6; // 6k

        @Override
        public InputStream stream(final byte[] value) {
            return new ByteArrayInputStream(value);
        }

        @Override
        public byte[] parse(final InputStream stream) {
            try {
                final var buffer = new byte[MAX_MESSAGE_SIZE];
                var count = 0;
                var b = 0;
                while ((b = stream.read()) != -1) {
                    buffer[count++] = (byte) b;
                    if (count >= MAX_MESSAGE_SIZE) {
                        // TODO throw an exception because the message was too big.
                        //      We need to have some proper runtime exception we can catch and turn into a TransactionResponse!
                        throw new RuntimeException("Too big message, boss.");
                    }
                }
                return buffer;
            } catch (IOException e) {
                // TODO I need to have some runtime exceptions that can be caught and turned into a TransactionResponse.
                throw new RuntimeException(e);
            }
        }
    }
}
