package com.hedera.hashgraph.token.entity;

import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.hapi.model.KeyList;

public interface Account {
    AccountID id();
    KeyList keys();
    long balance(); // Maybe have something more useful here for hbar that can convert tinybar etc.
}
