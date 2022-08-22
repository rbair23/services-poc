package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.app.SessionContext;
import com.hedera.hashgraph.app.workflows.ingest.IngestWorkflow;
import com.hedera.hashgraph.hapi.parsers.proto.TransactionBodyProtoParser;
import com.hedera.hashgraph.hapi.parsers.proto.base.SignedTransactionProtoParser;
import com.hedera.hashgraph.hapi.parsers.proto.base.TransactionProtoParser;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

import java.util.Objects;

final class TransactionMethod implements ServerCalls.UnaryMethod<byte[], byte[]> {
    /**
     * Per-thread shared resources are shared in a {@link SessionContext}. We store these
     * in a thread local, because we do not have control over the thread pool used
     * by the underlying gRPC server.
     */
    private static final ThreadLocal<SessionContext> SESSION_CONTEXT_THREAD_LOCAL =
            // TODO Use custom body parser to avoid parsing the OneOf!
            ThreadLocal.withInitial(() -> new SessionContext(
                        new TransactionProtoParser(),
                        new SignedTransactionProtoParser(),
                        new TransactionBodyProtoParser()));

    /**
     * The pipeline contains all the steps needed for handling the ingestion of a transaction.
     */
    private final IngestWorkflow pipeline;

    TransactionMethod(IngestWorkflow pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline);
    }

    @Override
    public void invoke(byte[] txBytes, StreamObserver<byte[]> responseObserver) {
        try {
            final var session = SESSION_CONTEXT_THREAD_LOCAL.get();
            final var responseBytes = pipeline.handleTransaction(session, txBytes);
            responseObserver.onNext(responseBytes); // Return this to the client
            responseObserver.onCompleted(); // Shut it down.
        } catch (Throwable th) {
            // TODO Log this. Something awful happened!!
            // TODO We should definitely have metrics for how often this is happening
            responseObserver.onError(th);
        }
    }
}
