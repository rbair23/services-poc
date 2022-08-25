package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.app.SessionContext;
import com.hedera.hashgraph.app.workflows.ingest.IngestWorkflow;
import com.hedera.hashgraph.app.workflows.query.QueryWorkflow;
import com.hedera.hashgraph.hapi.parser.QueryProtoParser;
import com.hedera.hashgraph.hapi.parser.TransactionBodyProtoParser;
import com.hedera.hashgraph.hapi.parser.base.SignedTransactionProtoParser;
import com.hedera.hashgraph.hapi.parser.base.TransactionProtoParser;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;

/**
 * Handles gRPC duties for processing {@link com.hedera.hashgraph.hapi.model.Query} gRPC calls. A single
 * instance of this class is used by all query threads in the node.
 */
@ThreadSafe
final class QueryMethod implements ServerCalls.UnaryMethod<byte[], byte[]> {
    /**
     * Per-thread shared resources are shared in a {@link SessionContext}. We store these
     * in a thread local, because we do not have control over the thread pool used
     * by the underlying gRPC server.
     */
    private static final ThreadLocal<SessionContext> SESSION_CONTEXT_THREAD_LOCAL =
            // TODO Use custom body parser to avoid parsing the OneOf!
            ThreadLocal.withInitial(() -> new SessionContext(
                        new QueryProtoParser(),
                        new TransactionProtoParser(),
                        new SignedTransactionProtoParser(),
                        new TransactionBodyProtoParser()));

    /**
     * The workflow contains all the steps needed for handling the query.
     */
    private final QueryWorkflow workflow;

    /**
     * Create a new QueryMethod. This is only called by the {@link HederaGrpcServiceBuilder}.
     * @param workflow a non-null {@link QueryWorkflow}
     */
    QueryMethod(@Nonnull QueryWorkflow workflow) {
        this.workflow = Objects.requireNonNull(workflow);
    }

    @Override
    public void invoke(byte[] queryBytes, StreamObserver<byte[]> responseObserver) {
        try {
            final var session = SESSION_CONTEXT_THREAD_LOCAL.get();
            final var responseBytes = workflow.handleQuery(session, queryBytes);
            responseObserver.onNext(responseBytes); // Return this to the client
            responseObserver.onCompleted(); // Shut it down.
        } catch (Throwable th) {
            // TODO Log this. Something awful happened!!
            // TODO We should definitely have metrics for how often this is happening
            responseObserver.onError(th);
        }
    }
}
