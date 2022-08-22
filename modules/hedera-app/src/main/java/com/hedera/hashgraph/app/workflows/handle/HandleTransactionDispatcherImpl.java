package com.hedera.hashgraph.app.workflows.handle;

import com.hedera.hashgraph.app.ServicesAccessor;
import com.hedera.hashgraph.base.*;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

import java.util.Objects;
import java.util.function.Supplier;

public class HandleTransactionDispatcherImpl implements HandleTransactionDispatcher {
    private final ServicesAccessor services;
    private final ThrottleAccumulator throttleAccumulator;
    private final Supplier<FeeAccumulator> feeAccumulatorSupplier;

    public HandleTransactionDispatcherImpl(ServicesAccessor services, ThrottleAccumulator throttleAccumulator, Supplier<FeeAccumulator> feeAccumulatorSupplier) {
        this.services = Objects.requireNonNull(services);
        this.throttleAccumulator = Objects.requireNonNull(throttleAccumulator);
        this.feeAccumulatorSupplier = Objects.requireNonNull(feeAccumulatorSupplier);
    }

    @Override
    public void dispatch(OneOf<TransactionBody.DataOneOfType, Object> transactionBodyData) {
        final var changeManager = new ChangeManager();
        final var feeAccumulator = feeAccumulatorSupplier.get();
        final var ctx = new HandleContext(changeManager, throttleAccumulator, feeAccumulator);
        final var kind = transactionBodyData.kind();
        switch (kind) {
            case FILE_CREATE -> services.fileService().transactionHandler()
                    .handleFileCreate(ctx, transactionBodyData.as());
            case CRYPTO_CREATE_ACCOUNT -> services.accountService().transactionHandler()
                    .handleAccountCreate(ctx, transactionBodyData.as());
            default ->
                    throw new RuntimeException("Unexpected kind " + kind);
        }
        // TODO Commit the change manager, and whatever else needs to be done...fees, whatever.
    }
}
