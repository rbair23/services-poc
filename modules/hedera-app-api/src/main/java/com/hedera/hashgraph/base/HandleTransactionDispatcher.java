package com.hedera.hashgraph.base;

import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

public interface HandleTransactionDispatcher {
    void dispatch(HandleContext ctx, OneOf<TransactionBody.DataOneOfType> transactionBodyData);
}
