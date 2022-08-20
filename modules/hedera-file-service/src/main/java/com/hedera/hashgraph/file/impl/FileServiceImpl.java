package com.hedera.hashgraph.file.impl;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.hedera.hashgraph.base.QueryHandler;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.file.FileEndpoint;
import com.hedera.hashgraph.file.FileService;
import com.hedera.hashgraph.file.impl.store.FileStore;

public class FileServiceImpl implements FileService {
	private final FileStore store;
	private final FileTransactionHandlerImpl txHandler;
	private final FileEndpointImpl endpoint;

	public FileServiceImpl(
			/* I should get passed the merkel internal representing FileService, on which I attach my own merkle node with my own types*/
			MerkleRegistry registry) {
		// todo how do I get the merkle stuff needed for the file store. I guess it has to come from the constructor.
		//      I have to take the "parent" and check if it has my node or not. If it does, then I don't have to
		//      create it, I can just pass it in. If it isn't there, I need to create it and add it.
		store = new FileStore(registry);
		txHandler = new FileTransactionHandlerImpl(store);
		endpoint = new FileEndpointImpl(txHandler);
	}

	@Override
	public FileEndpoint getEndpoint() {
		return endpoint;
	}

	@Override
	public TransactionHandler getTransactionHandler() {
		return txHandler;
	}

	@Override
	public QueryHandler getQueryHandler() {
		return null;
	}
}
