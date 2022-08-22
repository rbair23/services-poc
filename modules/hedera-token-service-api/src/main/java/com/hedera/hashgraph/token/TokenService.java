package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.Service;

public interface TokenService extends Service {
    TokenTransactionHandler transactionHandler();
}
