package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.hedera.hashgraph.token.TokenService;
import com.hedera.hashgraph.token.impl.store.AccountStore;

public class TokenServiceImpl implements TokenService {
	private final AccountStore store;
	private final TokenTransactionHandlerImpl txHandler;

	public TokenServiceImpl(
			/* I should get passed the merkel internal representing FileService, on which I attach my own merkle node with my own types*/
			MerkleRegistry registry) {
		// todo how do I get the merkle stuff needed for the file store. I guess it has to come from the constructor.
		//      I have to take the "parent" and check if it has my node or not. If it does, then I don't have to
		//      create it, I can just pass it in. If it isn't there, I need to create it and add it.
		store = new AccountStore(registry);
		txHandler = new TokenTransactionHandlerImpl(store);
	}

}
