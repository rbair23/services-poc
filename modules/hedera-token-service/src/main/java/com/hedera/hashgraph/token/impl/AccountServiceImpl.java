package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.QueryHandler;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.token.*;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.impl.store.AccountStore;
import com.swirlds.common.merkle.MerkleInternal;

public class AccountServiceImpl implements AccountService {
	private final AccountStore store;

	public AccountServiceImpl(
			/* I should get passed the merkel internal representing FileService, on which I attach my own merkle node with my own types*/
			MerkleInternal parent) {
		store = new AccountStore(null);
	}

	@Override
	public AccountEndpoint getEndpoint() {
		return null;
	}

	@Override
	public AccountTransactionHandler getTransactionHandler() {
		return null;
	}

	@Override
	public QueryHandler getQueryHandler() {
		return null;
	}

	@Override
	public Account lookupAccount(AccountID accountID) {
		return store.loadAccount(accountID);
	}
}
