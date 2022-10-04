package com.hedera.hashgraph.base;

import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

import java.util.function.Function;

public interface HandleTransactionDispatcher {
    void dispatch(HandleContext ctx, Function<String,States> statesAccessor, OneOf<TransactionBody.DataOneOfType> transactionBodyData);
}
