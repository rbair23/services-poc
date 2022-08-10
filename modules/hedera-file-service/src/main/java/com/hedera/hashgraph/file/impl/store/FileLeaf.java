package com.hedera.hashgraph.file.impl.store;

import com.hedera.hashgraph.file.model.FileID;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.common.merkle.MerkleLeaf;
import com.swirlds.common.merkle.impl.PartialMerkleLeaf;
import com.swirlds.common.merkle.utility.Keyed;

import java.io.IOException;

class FileLeaf extends PartialMerkleLeaf implements MerkleLeaf, Keyed<FileID> {

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
	public FileID getKey() {
		return null;
	}

	@Override
	public void setKey(final FileID fileID) {

	}
}
