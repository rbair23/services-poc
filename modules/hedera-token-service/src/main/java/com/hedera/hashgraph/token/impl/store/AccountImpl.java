package com.hedera.hashgraph.token.impl.store;

import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.hapi.model.KeyList;
import com.hedera.hashgraph.token.entity.Account;

record AccountImpl(AccountID id, KeyList keys, long balance) implements Account {
}
