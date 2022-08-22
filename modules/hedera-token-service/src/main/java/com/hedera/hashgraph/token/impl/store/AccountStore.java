package com.hedera.hashgraph.token.impl.store;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.hapi.model.Key;
import com.hedera.hashgraph.hapi.model.KeyList;
import com.hedera.hashgraph.token.entity.Account;
import com.swirlds.merkle.map.MerkleMap;

import java.util.Collections;

// NOTE: This is NOT exported from the module
public class AccountStore {
	private final MerkleMap<AccountID, AccountLeaf> mmap;

	public AccountStore(MerkleRegistry registry) {
		this.mmap = null; // todo use registry
	}

	public Account loadAccount(AccountID id) {
		// TODO validate id has the right shard/realm and isn't null.
//		return loadFileInfo(id.num());
		return new AccountImpl(id, new KeyList(Collections.singletonList(new Key(null))), 1000);
	}
}
