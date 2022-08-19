package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.QueryHandler;
import com.hedera.hashgraph.base.Service;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.token.entity.Account;

public interface AccountService extends Service {
	@Override
	public AccountEndpoint getEndpoint();

	@Override
	public AccountTransactionHandler getTransactionHandler();

	@Override
	public QueryHandler getQueryHandler();

	public Account lookupAccount(AccountID accountID);
}
