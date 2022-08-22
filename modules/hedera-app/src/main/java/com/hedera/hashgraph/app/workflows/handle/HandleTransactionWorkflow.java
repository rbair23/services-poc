package com.hedera.hashgraph.app.workflows.handle;

import com.hedera.hashgraph.base.HandleTransactionDispatcher;
import com.hedera.hashgraph.base.TransactionMetadata;
import com.swirlds.common.system.Round;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class HandleTransactionWorkflow {
    private final HandleTransactionDispatcher dispatcher;

    public HandleTransactionWorkflow(HandleTransactionDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void start(Round r) {
        final var itr = r.eventIterator();
        while (itr.hasNext()) {
            final var event = itr.next();
            final var txItr = event.consensusTransactionIterator();
            while (txItr.hasNext()) {
                final var platformTx = txItr.next();
                final Future<TransactionMetadata> future = platformTx.getMetadata();
                try {
                    final var md = future.get();
                    if (md.failed()) {
                        // TODO The transaction failed during pre-check, so charge and such but don't dispatch.
                    } else {
                        final var tx = md.transaction();
                        dispatcher.dispatch(tx.body().data());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
