package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.hapi.model.token.CryptoCreateTransactionBody;

// todo how to do rollback and commit? especially across modules
public interface AccountTransactionHandler {
    void handleAccountCreate(HandleContext ctx, CryptoCreateTransactionBody tx);
//	TransactionRecord createFile(ChangeManager cm, FileCreateTransactionBody tx);
}
