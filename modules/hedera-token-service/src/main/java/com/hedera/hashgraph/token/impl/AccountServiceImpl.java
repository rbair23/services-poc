package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.token.AccountService;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.impl.store.AccountStore;

public class AccountServiceImpl implements AccountService {
	private final AccountStore store;

	public AccountServiceImpl(
			/* I should get passed the merkel internal representing FileService, on which I attach my own merkle node with my own types*/
			MerkleRegistry registry) {
		store = new AccountStore(registry);
	}

	@Override
	public Account lookupAccount(AccountID accountID) {
		return store.loadAccount(accountID);
	}
}
