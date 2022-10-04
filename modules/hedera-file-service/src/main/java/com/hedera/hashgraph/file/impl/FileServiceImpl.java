package com.hedera.hashgraph.file.impl;

import com.hedera.hashgraph.base.state.StateRegistry;
import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.file.FileQueryHandler;
import com.hedera.hashgraph.file.FileService;
import com.hedera.hashgraph.file.FileTransactionHandler;
import com.hedera.hashgraph.file.impl.store.FileStore;
import edu.umd.cs.findbugs.annotations.NonNull;

public class FileServiceImpl implements FileService {
	private final FileStore store;
//	private final FileTransactionHandler txHandler;

	public FileServiceImpl(StateRegistry registry) {
		store = new FileStore();
//		txHandler = new FileTransactionHandlerImpl(store);
//		txHandler = null;
	}

	@Override
	@NonNull
	public FileTransactionHandler createTransactionHandler(@NonNull States states) {
		return new FileTransactionHandlerImpl(store);
	}

	@Override
	public FileQueryHandler createQueryHandler(States states) {
		return new FileQueryHandler() {
		};
	}
}
