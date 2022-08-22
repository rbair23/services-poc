package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.QueryHandler;
import com.hedera.hashgraph.base.Service;
import com.hedera.hashgraph.base.TransactionHandler;

public interface TokenService extends Service {
	@Override
	public TokenTransactionHandler getTransactionHandler();

	@Override
	public QueryHandler getQueryHandler();
}
