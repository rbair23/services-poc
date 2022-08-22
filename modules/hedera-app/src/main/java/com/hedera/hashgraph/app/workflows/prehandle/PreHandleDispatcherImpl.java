package com.hedera.hashgraph.app.workflows.prehandle;

import com.hedera.hashgraph.base.PreHandleDispatcher;
import com.hedera.hashgraph.app.ServicesAccessor;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

import java.util.Objects;

public final class PreHandleDispatcherImpl implements PreHandleDispatcher {
    private final ServicesAccessor services;

    public PreHandleDispatcherImpl(ServicesAccessor services) {
        this.services = Objects.requireNonNull(services);
    }

    public void dispatch(OneOf<TransactionBody.DataOneOfType, Object> transactionBodyData) {
        final var kind = transactionBodyData.kind();
        switch (kind) {
            case FILE_CREATE -> services.fileService().preHandler().preHandleFileCreate(transactionBodyData.as());
            case CRYPTO_CREATE_ACCOUNT -> services.accountService().preHandler().preHandleAccountCreate(transactionBodyData.as());
            default ->
                    throw new RuntimeException("Unexpected kind " + kind);
        }
    }
}
