package com.hedera.hashgraph.token.impl.store;

import com.hedera.hashgraph.base.state.StateRegistry;
import com.hedera.hashgraph.base.state.States;

public class TokenStore {
    public TokenStore(States states) {
//        this.accountState = states.get(ACCOUNT_STORE);
//        assert this.accountState != null : "States must throw IAE if not found, and not return null";
    }

    public static void register(StateRegistry registry) {
//        registry.<Long, MerkleAccount>getOrRegister(ACCOUNT_STORE, (builder, existing) -> {
//            if (existing.isEmpty()) {
//                return builder.<Long, MerkleAccount>inMemory(ACCOUNT_STORE).build();
//            } else {
//                // If there was anything to migrate, I'd do it here
//                return existing.get();
//            }
//        });
    }
}
