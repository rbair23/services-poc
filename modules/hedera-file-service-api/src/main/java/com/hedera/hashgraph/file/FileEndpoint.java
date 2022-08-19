package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.Endpoint;
import com.hedera.hashgraph.hapi.model.TransactionBody;
import com.hedera.hashgraph.hapi.model.base.ResponseCodeEnum;

public interface FileEndpoint extends Endpoint {
	ResponseCodeEnum handleFileCreateTransaction(TransactionBody body);
}
