package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.QueryHandler;
import com.hedera.hashgraph.base.Service;
import com.hedera.hashgraph.base.TransactionHandler;

public interface FileService extends Service {
	@Override
	public TransactionHandler getTransactionHandler();

	@Override
	public QueryHandler getQueryHandler();
}
