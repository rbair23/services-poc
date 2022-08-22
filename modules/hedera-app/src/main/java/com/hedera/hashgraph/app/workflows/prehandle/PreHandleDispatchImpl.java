package com.hedera.hashgraph.app.workflows.prehandle;

import com.hedera.hashgraph.base.PreHandleDispatch;
import com.hedera.hashgraph.app.ServicesAccessor;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

import java.util.Objects;

public final class PreHandleDispatchImpl implements PreHandleDispatch {
    private final ServicesAccessor services;

    public PreHandleDispatchImpl(ServicesAccessor services) {
        this.services = Objects.requireNonNull(services);
    }

    public void dispatch(OneOf<TransactionBody.DataOneOfType, Object> transactionBodyData) {
        final var kind = transactionBodyData.kind();
        switch (kind) {
            case FileCreate -> services.fileService().preHandler().preHandleFileCreate(transactionBodyData.as());
            case CryptoCreateAccount -> services.accountService().preHandler().preHandleAccountCreate(transactionBodyData.as());
            default ->
                    throw new RuntimeException("Unexpected kind " + kind);
        }
    }
}
