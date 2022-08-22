package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.TransactionMetadata;
import com.hedera.hashgraph.hapi.model.token.CryptoCreateTransactionBody;

public interface AccountPreHandler {
    TransactionMetadata preHandleAccountCreate(CryptoCreateTransactionBody tx);
}
