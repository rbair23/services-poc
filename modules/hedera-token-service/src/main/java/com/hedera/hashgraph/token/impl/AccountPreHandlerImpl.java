package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.TransactionMetadata;
import com.hedera.hashgraph.hapi.model.token.CryptoCreateTransactionBody;
import com.hedera.hashgraph.token.AccountPreHandler;

public class AccountPreHandlerImpl implements AccountPreHandler {
    @Override
    public TransactionMetadata preHandleAccountCreate(CryptoCreateTransactionBody tx) {
        return null;
    }
}
