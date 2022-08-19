package com.hedera.hashgraph.token.impl.store;

import com.hedera.hashgraph.base.Store;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.hapi.model.Key;
import com.hedera.hashgraph.hapi.model.KeyList;
import com.hedera.hashgraph.hapi.model.file.FileCreateTransactionBody;
import com.hedera.hashgraph.hapi.model.file.FileInfo;
import com.hedera.hashgraph.token.entity.Account;
import com.swirlds.merkle.map.MerkleMap;

// NOTE: This is NOT exported from the module
public class AccountStore implements Store {
	private final MerkleMap<AccountID, AccountLeaf> mmap;

	public AccountStore(MerkleMap<AccountID, AccountLeaf> mmap) {
		this.mmap = mmap; // todo validate
	}

	public Account loadAccount(AccountID id) {
		// TODO validate id has the right shard/realm and isn't null.
//		return loadFileInfo(id.num());
		return new AccountImpl(id, new KeyList(new Key(null)), 1000);
	}
}
