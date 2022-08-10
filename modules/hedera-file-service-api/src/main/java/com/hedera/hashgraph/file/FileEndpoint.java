package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.Endpoint;
import com.hedera.hashgraph.base.model.ResponseCode;
import com.hedera.hashgraph.base.model.TransactionBody;

public interface FileEndpoint extends Endpoint {
	ResponseCode handleFileCreateTransaction(TransactionBody body);
}
