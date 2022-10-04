package com.hedera.hashgraph.app.workflows.handle;

import com.hedera.hashgraph.app.record.RecordBuilderImpl;
import com.hedera.hashgraph.app.record.RecordStreamManager;
import com.hedera.hashgraph.app.state.HederaState;
import com.hedera.hashgraph.base.*;
import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.token.CryptoService;
import com.swirlds.common.system.Round;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.hedera.hashgraph.app.Hedera.CRYPTO_SERVICE;

public class HandleTransactionWorkflow {
    private final @Nonnull Supplier<HederaState> workingStateAccessor;
    private final @Nonnull CryptoService cryptoService;
    private final @Nonnull HandleTransactionDispatcher dispatcher;
    private final @Nonnull ThrottleAccumulator throttleAccumulator;
    private final @Nonnull RecordStreamManager recordStreamManager;
    private final @Nonnull Supplier<FeeAccumulator> feeAccumulatorFactory;

    public HandleTransactionWorkflow(
            @Nonnull Supplier<HederaState> workingStateAccessor,
            @Nonnull CryptoService cryptoService,
            @Nonnull HandleTransactionDispatcher dispatcher,
            @Nonnull ThrottleAccumulator throttleAccumulator,
            @Nonnull RecordStreamManager recordStreamManager,
            @Nonnull Supplier<FeeAccumulator> feeAccumulatorFactory) {
        this.workingStateAccessor = Objects.requireNonNull(workingStateAccessor);
        this.cryptoService = Objects.requireNonNull(cryptoService);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.throttleAccumulator = Objects.requireNonNull(throttleAccumulator);
        this.recordStreamManager = Objects.requireNonNull(recordStreamManager);
        this.feeAccumulatorFactory = Objects.requireNonNull(feeAccumulatorFactory);
    }

    public void start(@Nonnull Round r) {
        final var workingState = workingStateAccessor.get();
        final var itr = r.eventIterator();
        while (itr.hasNext()) {
            final var event = itr.next();
            final var txItr = event.consensusTransactionIterator();
            while (txItr.hasNext()) {
                final var platformTx = txItr.next();
                final Future<TransactionMetadata> future = platformTx.getMetadata();
                try {
                    final var md = future.get();
                    if (!md.failed()) {
                        // Create fresh for each transaction
                        final var feeAccumulator = feeAccumulatorFactory.get();
                        final var recordBuilder = new RecordBuilderImpl();
                        final AtomicLong nextId = new AtomicLong(workingState.getNextEntityId());
                        final var statesAccessor = new StatesAccessor(workingState);
                        final var ctx = new HandleContext(
                                nextId::getAndIncrement,
                                throttleAccumulator,
                                feeAccumulator,
                                recordBuilder);

                        // Dispatch the transaction. If it fails, it will throw an exception.
                        final var tx = md.transaction();
                        dispatcher.dispatch(ctx, statesAccessor, tx.body().data());

                        // Call the finalize method
                        final var cryptoStates = statesAccessor.apply(CRYPTO_SERVICE);
                        final var cryptoTxHandler = cryptoService.createTransactionHandler(cryptoStates);
                        cryptoTxHandler.finalizeTransfers(feeAccumulator, recordBuilder);

                        // If we got here, then we are ready to COMMIT everything to state. So go ahead and commit
                        // the states, and also the next entity ID or other state that may have changed
                        workingState.setNextEntityId(nextId.get());

                        // Output a record into the record manager from the record builder
                        recordStreamManager.submit(recordBuilder.build());
                    } else {
                        // TODO The transaction failed during pre-check, so charge and such but don't dispatch.
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static final class StatesAccessor implements Function<String, States> {
        private final HederaState state;
        private final Map<String, States> statesMap = new HashMap<>();

        StatesAccessor(HederaState state) {
            this.state = state;
        }

        @Override
        public States apply(String serviceName) {
            return statesMap.computeIfAbsent(serviceName, k -> state.createStates(serviceName));
        }
    }
}
