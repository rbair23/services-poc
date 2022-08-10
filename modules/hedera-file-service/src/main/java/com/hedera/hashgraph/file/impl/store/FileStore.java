package com.hedera.hashgraph.file.impl.store;

import com.hedera.hashgraph.base.Store;
import com.hedera.hashgraph.file.model.FileCreateTransactionBody;
import com.hedera.hashgraph.file.model.FileID;
import com.hedera.hashgraph.file.model.FileInfo;
import com.swirlds.merkle.map.MerkleMap;

// NOTE: This is NOT exported from the module
public class FileStore implements Store {
	private final MerkleMap<FileID, FileLeaf> mmap;

	public FileStore(MerkleMap<FileID, FileLeaf> mmap) {
		this.mmap = mmap; // todo validate
	}

	public FileInfo loadFileInfo(FileID id) {
		// TODO validate id has the right shard/realm and isn't null.
		return loadFileInfo(id.num());
	}

	public FileInfo loadFileInfo(long fileNum) {
		// TODO look it up in the backing merkle map / virtual map and create a FileInfo with the
		// data from the leaf
		return null;
	}

	public FileInfo createFile(FileCreateTransactionBody body) {
		// TODO do the stuff.
		return null;
	}

	public void saveFile(FileInfo info) {
		// TODO stuff
	}
}
