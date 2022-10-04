package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.state.StateRegistry;
import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.token.TokenQueryHandler;
import com.hedera.hashgraph.token.TokenService;
import com.hedera.hashgraph.token.TokenTransactionHandler;
import com.hedera.hashgraph.token.impl.store.TokenStore;
import edu.umd.cs.findbugs.annotations.NonNull;

public class TokenServiceImpl implements TokenService {
    public TokenServiceImpl(
            @SuppressWarnings("rawtypes") StateRegistry registry) {
        // Let the store handle migration or genesis work
        TokenStore.register(registry);
    }

    @NonNull
    @Override
    public TokenTransactionHandler createTransactionHandler(@NonNull States states) {
        final var store = new TokenStore(states);
        return new TokenTransactionHandlerImpl(store);
    }

    @NonNull
    @Override
    public TokenQueryHandler createQueryHandler(@NonNull States states) {
        final var store = new TokenStore(states);
        return new TokenQueryHandlerImpl(store);
    }
}
