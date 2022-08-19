package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.QueryHandler;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.token.TokenTransactionHandler;
import com.hedera.hashgraph.token.impl.store.AccountStore;
import com.hedera.hashgraph.token.TokenEndpoint;
import com.hedera.hashgraph.token.TokenService;
import com.swirlds.common.merkle.MerkleInternal;

public class TokenServiceImpl implements TokenService {
	private final AccountStore store;
	private final TokenTransactionHandlerImpl txHandler;
	private final TokenEndpointImpl endpoint;

	public TokenServiceImpl(
			/* I should get passed the merkel internal representing FileService, on which I attach my own merkle node with my own types*/
			MerkleInternal parent) {
		// todo how do I get the merkle stuff needed for the file store. I guess it has to come from the constructor.
		//      I have to take the "parent" and check if it has my node or not. If it does, then I don't have to
		//      create it, I can just pass it in. If it isn't there, I need to create it and add it.
		store = new AccountStore(null);
		txHandler = new TokenTransactionHandlerImpl(store);
		endpoint = new TokenEndpointImpl(txHandler);
	}

	@Override
	public TokenEndpoint getEndpoint() {
		return endpoint;
	}

	@Override
	public TokenTransactionHandler getTransactionHandler() {
		return txHandler;
	}

	@Override
	public QueryHandler getQueryHandler() {
		return null;
	}
}
