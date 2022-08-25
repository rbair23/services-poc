package com.hedera.hashgraph.app.workflows.handle;

import com.hedera.hashgraph.base.*;
import com.swirlds.common.system.Round;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class HandleTransactionWorkflow {
    private final HandleTransactionDispatcher dispatcher;
    private final ThrottleAccumulator throttleAccumulator;
    private final Supplier<FeeAccumulator> feeAccumulatorSupplier;

    public HandleTransactionWorkflow(
            @Nonnull HandleTransactionDispatcher dispatcher,
            @Nonnull ThrottleAccumulator throttleAccumulator,
            @Nonnull Supplier<FeeAccumulator> feeAccumulatorSupplier) {
        this.dispatcher = dispatcher;
        this.throttleAccumulator = Objects.requireNonNull(throttleAccumulator);
        this.feeAccumulatorSupplier = Objects.requireNonNull(feeAccumulatorSupplier);
    }

    public void start(@Nonnull Round r) {
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
                        final var changeManager = new ChangeManager();
                        final var feeAccumulator = feeAccumulatorSupplier.get();
                        final var ctx = new HandleContext(changeManager, throttleAccumulator, feeAccumulator);
                        final var tx = md.transaction();
                        dispatcher.dispatch(ctx, tx.body().data());
                        // TODO Commit the change manager, and whatever else needs to be done...fees, whatever.
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
