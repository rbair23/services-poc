package com.hedera.hashgraph.app.grpc;

//import io.grpc.MethodDescriptor;
//import io.helidon.grpc.core.MarshallerSupplier;
//import io.helidon.grpc.server.ServiceDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Convenient builder API for constructing GRPC Service definitions. The {@code HederaGrpcServiceBuilder}
 * is capable of constructing service definitions for {@link com.hedera.hashgraph.hapi.model.base.Transaction}
 * based calls using the {@link #transaction(String)} method, or {@link com.hedera.hashgraph.hapi.model.Query}
 * based calls using the {@link #query(String)} method.
 *
 * <p>Every gRPC service definition needs to define, per service method definition, the "marshaller" to use
 * for marshalling and unmarshalling binary data sent in the protocol. Usually this is some kind of protobuf
 * parser. In our case, we simply read a byte array from the {@link InputStream} and pass the array raw to
 * the appropriate workflow implementation {@link com.hedera.hashgraph.app.workflows.ingest.IngestWorkflow}
 * or {@link com.hedera.hashgraph.app.workflows.query.QueryWorkflow}, so they can do the protobuf parsing.
 * We do this to segregate the code. This class is <strong>only</strong> responsible for the gRPC call,
 * the workflows are responsible for working with protobuf.
 *
 * <p>Instances of the builder are created by the {@link HederaGrpcHandler} for each unique Service definition.
 */
@NotThreadSafe
public final class HederaGrpcServiceBuilder {
    /**
     * Create a single JVM-wide Marshaller instance that simply reads/writes byte arrays to/from
     * {@link InputStream}s. This class is totally thread safe because it does not reuse byte arrays.
     * If we get more sophisticated and reuse byte array buffers, we will need to use a
     * {@link ThreadLocal} to make sure we have a unique byte array buffer for each request.
     */
//    private static final NoopMarshaller NOOP_MARSHALLER = new NoopMarshaller();

    /**
     * Create a single instance of the marshaller supplier to provide to every gRPC method registered
     * with the system. We only need the one, and it always returns the same NoopMarshaller instance.
     * This is fine to use with multiple app instances within the same JVM.
     */
//    private static final MarshallerSupplier MARSHALLER_SUPPLIER = new MarshallerSupplier() {
//        @Override
//        public <T> MethodDescriptor.Marshaller<T> get(Class<T> clazz) {
//            //noinspection unchecked
//            return (MethodDescriptor.Marshaller<T>) NOOP_MARSHALLER;
//        }
//    };

    /**
     * The name of the service we are building.
     */
    private final String serviceName;

    /**
     * A single instance of {@link TransactionMethod} containing the gRPC logic for invoking a
     * {@link com.hedera.hashgraph.app.workflows.ingest.IngestWorkflow}. This single instance can
     * be reused across all threads in a single app instance.
     */
    private final TransactionMethod transactionMethod;

    /**
     * A single instance of {@link QueryMethod} containing the gRPC logic for invoking a
     * {@link com.hedera.hashgraph.app.workflows.query.QueryWorkflow}. This single instance can
     * be reused across all threads in a single app instance.
     */
    private final QueryMethod queryMethod;

    /**
     * The set of transaction method names that need corresponding service method definitions generated.
     */
    private final Set<String> txMethodNames = new HashSet<>();

    /**
     * The set of query method names that need corresponding service method definitions generated.
     */
    private final Set<String> queryMethodNames = new HashSet<>();

    /**
     * Creates a new builder. This is only called by the {@link HederaGrpcHandler}.
     *
     * @param serviceName The name of the service. Cannot be null or blank.
     * @param transactionMethod The {@link TransactionMethod} to delegate to for handling transaction calls.
     *                          This cannot be null.
     * @param queryMethod The {@link QueryMethod} to delegate to for handling query calls. Cannot be null.
     */
    HederaGrpcServiceBuilder(
            @Nonnull String serviceName,
            @Nonnull TransactionMethod transactionMethod,
            @Nonnull QueryMethod queryMethod) {
        this.transactionMethod = Objects.requireNonNull(transactionMethod);
        this.queryMethod = Objects.requireNonNull(queryMethod);
        this.serviceName = Objects.requireNonNull(serviceName);
        if (serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName cannot be blank");
        }
    }

    /**
     * Register the creation of a new gRPC method for handling transactions with the given name.
     * This call is idempotent.
     *
     * @param methodName The name of the transaction method. Cannot be null or blank.
     * @return A reference to the builder.
     */
    public HederaGrpcServiceBuilder transaction(@Nonnull String methodName) {
        if (Objects.requireNonNull(methodName).isBlank()) {
            throw new IllegalArgumentException("The gRPC method name cannot be blank");
        }

        txMethodNames.add(methodName);
        return this;
    }

    /**
     * Register the creation of a new gRPC method for handling queries with the given name.
     * This call is idempotent.
     *
     * @param methodName The name of the query method. Cannot be null or blank.
     * @return A reference to the builder.
     */
    public HederaGrpcServiceBuilder query(@Nonnull String methodName) {
        if (Objects.requireNonNull(methodName).isBlank()) {
            throw new IllegalArgumentException("The gRPC method name cannot be blank");
        }

        queryMethodNames.add(methodName);
        return this;
    }

//    /**
//     * Build a gRPC {@link ServiceDescriptor} for each transaction and query method registered with this builder.
//     *
//     * @return a non-null {@link ServiceDescriptor}.
//     */
//    public ServiceDescriptor build() {
//        final var builder = ServiceDescriptor.builder(null, serviceName);
//        txMethodNames.forEach(methodName -> builder.unary(
//                methodName,
//                transactionMethod,
//                rules -> rules.marshallerSupplier(MARSHALLER_SUPPLIER)));
//        queryMethodNames.forEach(methodName -> builder.unary(
//                methodName,
//                queryMethod,
//                rules -> rules.marshallerSupplier(MARSHALLER_SUPPLIER)));
//        return builder.build();
//    }

    /**
     * An implementation of a gRPC marshaller which does nothing but pass through byte arrays.
     * A single implementation of this class is designed to be used by multiple threads, including
     * by multiple app instances within a single JVM!
     */
//    @ThreadSafe
//    private static final class NoopMarshaller implements MethodDescriptor.Marshaller<byte[]> {
//        // TODO This value should be retrieved from config. We need the new config API from platform base.
//        //      Oh. When we do have that config, then maybe the NoopMarshaller will have to change to being
//        //      a single instance within the app instance rather than a single instance across the JVM.
//        private static final int MAX_MESSAGE_SIZE = 1024 * 6; // 6k
//
//        @Override
//        public InputStream stream(final byte[] value) {
//            // Simply wrap the supplied byte array.
//            // TODO Should there be a defensive copy made here? What is the API contract here? I'm assuming not.
//            //      If we used something more rigorous than a byte[], such as some kind of ByteBuffer or maybe
//            //      our own type of buffer, then we could mark it as "immutable" so it is safe to pass around
//            //      without making copies.
//            return new ByteArrayInputStream(value);
//        }
//
//        @Override
//        public byte[] parse(final InputStream stream) {
//            try {
//                // TODO I could have more sophisticated logic to read a chunk at a time from the stream instead of
//                //      a single byte at a time. Just remember it may only read a subset of the total buffer size
//                //      in a single call!
//                final var buffer = new byte[MAX_MESSAGE_SIZE];
//                var count = 0;
//                var b = 0;
//                while ((b = stream.read()) != -1) {
//                    buffer[count++] = (byte) b;
//                    if (count >= MAX_MESSAGE_SIZE) {
//                        // TODO throw an exception because the message was too big.
//                        //      What kind of response is given to the user in this case?
//                        throw new RuntimeException("Too big message, boss.");
//                    }
//                }
//                return buffer;
//            } catch (IOException e) {
//                // TODO Do I need a more specific exception type?
//                throw new RuntimeException(e);
//            }
//        }
//    }
}
