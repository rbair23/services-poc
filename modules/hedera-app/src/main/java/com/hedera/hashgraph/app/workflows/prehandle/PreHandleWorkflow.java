package com.hedera.hashgraph.app.workflows.prehandle;

import com.hedera.hashgraph.base.PreHandleDispatcher;
import com.hedera.hashgraph.app.SessionContext;
import com.hedera.hashgraph.app.workflows.ingest.IngestChecker;
import com.hedera.hashgraph.app.workflows.ingest.PreCheckException;
import com.hedera.hashgraph.base.TransactionMetadata;
import com.hedera.hashgraph.hapi.model.base.ResponseCodeEnum;
import com.hedera.hashgraph.hapi.parser.QueryProtoParser;
import com.hedera.hashgraph.hapi.parser.TransactionBodyProtoParser;
import com.hedera.hashgraph.hapi.parser.base.SignedTransactionProtoParser;
import com.hedera.hashgraph.hapi.parser.base.TransactionProtoParser;
import com.hedera.hashgraph.token.CryptoQueryHandler;
import com.hedera.hashgraph.token.CryptoService;
import com.swirlds.common.system.events.Event;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class PreHandleWorkflow {
    /**
     * Per-thread shared resources are shared in a {@link SessionContext}. We store these
     * in a thread local, because we do not have control over the thread pool used
     * by the underlying gRPC server.
     */
    private static final ThreadLocal<SessionContext> SESSION_CONTEXT_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> new SessionContext(
                    new QueryProtoParser(),
                    new TransactionProtoParser(),
                    new SignedTransactionProtoParser(),
                    new TransactionBodyProtoParser()));

    private final ExecutorService exe;
    private final Supplier<CryptoQueryHandler> query;
    private final IngestChecker checker;
    private final PreHandleDispatcher dispatcher;

    public PreHandleWorkflow(
            ExecutorService exe,
            Supplier<CryptoQueryHandler> query,
            IngestChecker ingestChecker,
            PreHandleDispatcher dispatcher) {
        this.exe = Objects.requireNonNull(exe);
        this.query = Objects.requireNonNull(query);
        this.checker = Objects.requireNonNull(ingestChecker);
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }

    public void start(Event event) {
        // Each transaction in the event will go through pre-handle using a background thread
        // from the executor service. The Future representing that work is stored on the
        // platform transaction. The HandleTransactionWorkflow will pull this future back
        // out and use it to block until the pre handle work is done, if needed.
        final var itr = event.transactionIterator();
        while (itr.hasNext()) {
            final var platformTx = itr.next();
            final var future = exe.submit(() -> preHandle(platformTx));
            platformTx.setMetadata(future);
        }
    }

    private TransactionMetadata preHandle(com.swirlds.common.system.transaction.Transaction platformTx) {
        try {
            final var ctx = SESSION_CONTEXT_THREAD_LOCAL.get();
            final var txBytes = platformTx.getContents();

            // 0. Parse the transaction object from the txBytes (protobuf)
            final var tx = ctx.txParser().parse(txBytes);

            // 1. Parse and validate the signed transaction
            final var signedTransaction = ctx.signedParser().parse(tx.signedTransactionBytes());
            checker.checkSignedTransaction(signedTransaction);

            // 2. Parse and validate the TransactionBody.
            final var txBody = ctx.txBodyParser().parse(signedTransaction.bodyBytes());
            final var accountOpt = query.get().getAccountById(txBody.transactionID().accountID());
            if (accountOpt.isEmpty()) {
                // This is an error condition. No account!
                throw new PreCheckException(ResponseCodeEnum.INVALID_ACCOUNT_ID, "Account missing");
            }
            final var account = accountOpt.get();
            checker.checkTransactionBody(txBody, account);

            // 3. Validate signature
            final var key = account.key().orElse(null);
            checker.checkSignatures(tx.signedTransactionBytes(), signedTransaction.sigMap(), key);

            // 4. If signatures all check out, then check the throttles. Ya, I'd like to check
            //    throttles way back on step 1, but we need to verify whether the account is
            //    a privileged account (less than 0.0.100) and if so skip throttles. Without
            //    a way to authenticate the payload any earlier than step 3, we have to do it now.
            final var kind = txBody.data().kind();
            checker.checkThrottles(kind);

            // Now that all the standard "ingest" checks are done, delegate to the appropriate service module
            // to do any service-specific pre-checks.
            dispatcher.dispatch(txBody.data());

            // Looks like we've done all we can and still haven't encountered any kind of problem, so we can
            // go ahead and create and return the transaction metadata.
            // TODO Need to provide a way for service modules to do their own preloading and save it in the md
            return new TransactionMetadata(tx);
        } catch (PreCheckException preCheckException) {
            // TODO Actually we should have a more specific kind of metadata here maybe? And definitely don't log.
            return new TransactionMetadata.UnknownErrorTransactionMetadata(preCheckException);
        } catch (Throwable th) {
            // Some unknown and unexpected failure happened. If this was non-deterministic, I could end up with
            // an ISS. It is critical that I log whatever happened, because we should have caught all legitimate
            // failures in another catch block.
            // TODO Log it.
            return new TransactionMetadata.UnknownErrorTransactionMetadata(th);
        }
    }
}
