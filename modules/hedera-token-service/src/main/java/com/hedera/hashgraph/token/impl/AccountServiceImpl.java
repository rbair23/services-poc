package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.token.AccountPreHandler;
import com.hedera.hashgraph.token.AccountService;
import com.hedera.hashgraph.token.AccountTransactionHandler;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.impl.store.AccountStore;

public class AccountServiceImpl implements AccountService {
	private final AccountStore store;
	private final AccountPreHandler preHandler;
	private final AccountTransactionHandler txHandler;

	public AccountServiceImpl(
			/* I should get passed the merkel internal representing FileService, on which I attach my own merkle node with my own types*/
			MerkleRegistry registry) {
		store = new AccountStore(registry);
		preHandler = new AccountPreHandlerImpl();
		txHandler =  null;
	}

	@Override
	public AccountTransactionHandler transactionHandler() {
		return txHandler;
	}

	@Override
	public AccountPreHandler preHandler() {
		return preHandler;
	}

	@Override
	public Account lookupAccount(AccountID accountID) {
		return store.loadAccount(accountID);
	}
}
