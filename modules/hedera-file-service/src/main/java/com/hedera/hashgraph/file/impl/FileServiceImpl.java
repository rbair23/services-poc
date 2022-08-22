package com.hedera.hashgraph.file.impl;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.hedera.hashgraph.file.FilePreHandler;
import com.hedera.hashgraph.file.FileService;
import com.hedera.hashgraph.file.FileTransactionHandler;
import com.hedera.hashgraph.file.impl.store.FileStore;

public class FileServiceImpl implements FileService {
	private final FileStore store;
	private final FileTransactionHandler txHandler;
	private final FilePreHandler preHandler;

	public FileServiceImpl(MerkleRegistry registry) {
		store = new FileStore(registry);
		txHandler = new FileTransactionHandlerImpl(store);
		preHandler = new FilePreHandlerImpl();
	}

	@Override
	public FilePreHandler preHandler() {
		return preHandler;
	}

	@Override
	public FileTransactionHandler transactionHandler() {
		return txHandler;
	}
}
