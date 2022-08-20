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
	public AccountTransactionHandler getTransactionHandler(); // TODO ALWAYS works on the current working state

	@Override
	public QueryHandler getQueryHandler(); // TODO ALWAYS works on last signed state

	/**
	 * TODO CRITICAL THIS MUST KNOW WHETHER TO LOOK UP AN ACCOUNT ON LAST SIGNED STATE OR CURRENT WORKING STATE
	 * @param accountID
	 * @return
	 */
	public Account lookupAccount(AccountID accountID);
}
