package com.hedera.hashgraph.token.impl.store;

import com.hedera.hashgraph.hapi.model.AccountID;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.common.merkle.MerkleLeaf;
import com.swirlds.common.merkle.impl.PartialMerkleLeaf;
import com.swirlds.common.merkle.utility.Keyed;

import java.io.IOException;

class AccountLeaf extends PartialMerkleLeaf implements MerkleLeaf, Keyed<AccountID> {

	@Override
	public MerkleLeaf copy() {
		return null;
	}

	@Override
	public long getClassId() {
		return 0;
	}

	@Override
	public void deserialize(final SerializableDataInputStream serializableDataInputStream,
			final int i) throws IOException {

	}

	@Override
	public void serialize(final SerializableDataOutputStream serializableDataOutputStream) throws IOException {

	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public AccountID getKey() {
		return null;
	}

	@Override
	public void setKey(final AccountID accountID) {

	}
}
