package com.hedera.hashgraph.app.workflows.handle;

import com.hedera.hashgraph.app.ServicesAccessor;
import com.hedera.hashgraph.base.HandleTransactionDispatcher;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

import java.util.Objects;

public class HandleTransactionDispatcherImpl implements HandleTransactionDispatcher {
    private final ServicesAccessor services;

    public HandleTransactionDispatcherImpl(ServicesAccessor services) {
        this.services = Objects.requireNonNull(services);
    }

    @Override
    public void dispatch(OneOf<TransactionBody.DataOneOfType, Object> transactionBodyData) {
        final var kind = transactionBodyData.kind();
//        switch (kind) {
//            case FileCreate -> services.fileService().transactionHandler().handleFileCreate(transactionBodyData.as());
//            case CryptoCreateAccount -> services.accountService().transactionHandler().handleAccountCreate(transactionBodyData.as());
//            default ->
//                    throw new RuntimeException("Unexpected kind " + kind);
//        }
    }
}
