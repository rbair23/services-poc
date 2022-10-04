package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.state.StateRegistry;
import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.token.CryptoQueryHandler;
import com.hedera.hashgraph.token.CryptoService;
import com.hedera.hashgraph.token.CryptoTransactionHandler;
import com.hedera.hashgraph.token.impl.store.AccountStore;
import edu.umd.cs.findbugs.annotations.NonNull;

public class CryptoServiceImpl implements CryptoService {

	public CryptoServiceImpl(
			@SuppressWarnings("rawtypes") StateRegistry registry) {
		// Let the store handle migration or genesis work
		AccountStore.register(registry);
	}

	@Override
	public @NonNull CryptoTransactionHandler createTransactionHandler(@NonNull States states) {
		final var store = new AccountStore(states);
		return new CryptoTransactionHandlerImpl(store);
	}

	@Override
	public @NonNull CryptoQueryHandler createQueryHandler(@NonNull States states) {
		final var store = new AccountStore(states);
		return new CryptoQueryHandlerImpl(store);
	}
}
