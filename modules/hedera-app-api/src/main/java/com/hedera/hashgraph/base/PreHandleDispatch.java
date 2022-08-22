package com.hedera.hashgraph.base;

import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.TransactionBody;

public interface PreHandleDispatch {
    void dispatch(OneOf<TransactionBody.DataOneOfType, Object> transactionBodyData);
}
