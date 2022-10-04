package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.hapi.model.token.TokenGetInfoQuery;
import com.hedera.hashgraph.hapi.model.token.TokenGetNftInfoQuery;
import com.hedera.hashgraph.token.TokenQueryHandler;
import com.hedera.hashgraph.token.impl.store.TokenStore;
import edu.umd.cs.findbugs.annotations.NonNull;

public class TokenQueryHandlerImpl implements TokenQueryHandler {
    private final TokenStore store;

    TokenQueryHandlerImpl(TokenStore store) {
        this.store = store;
    }

    @Override
    public void getTokenInfo(@NonNull TokenGetInfoQuery query) {

    }

    @Override
    public void getTokenNftInfo(@NonNull TokenGetNftInfoQuery query) {

    }
}
