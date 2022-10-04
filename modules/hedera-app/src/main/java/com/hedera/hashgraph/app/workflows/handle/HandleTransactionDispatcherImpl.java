package com.hedera.hashgraph.app.workflows.handle;

import com.hedera.hashgraph.app.ServicesAccessor;
import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.base.HandleTransactionDispatcher;
import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public class HandleTransactionDispatcherImpl implements HandleTransactionDispatcher {
    private final ServicesAccessor services;

    public HandleTransactionDispatcherImpl(@Nonnull ServicesAccessor services) {
        this.services = Objects.requireNonNull(services);
    }

    @Override
    public void dispatch(
            @Nonnull HandleContext ctx,
            @Nullable Function<String, States> statesAccessor,
            @Nonnull OneOf<TransactionBody.DataOneOfType> transactionBodyData) {
        final var kind = transactionBodyData.kind();
        switch (kind) {
//            case FILE_CREATE -> services.fileService().transactionHandler()
//                    .handleFileCreate(ctx, transactionBodyData.as());
//            case CRYPTO_CREATE_ACCOUNT -> services.cryptoService().transactionHandler()
//                    .handleAccountCreate(ctx, transactionBodyData.as());
            default ->
                    // TODO Maybe use a registration method instead of this...
                    throw new RuntimeException("Unexpected kind " + kind);
        }
    }
}
