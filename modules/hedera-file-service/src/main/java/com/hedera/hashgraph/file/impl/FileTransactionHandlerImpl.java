package com.hedera.hashgraph.file.impl;

import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.file.FileTransactionHandler;
import com.hedera.hashgraph.file.impl.store.FileStore;
import com.hedera.hashgraph.hapi.model.TransactionRecord;
import com.hedera.hashgraph.hapi.model.file.FileCreateTransactionBody;
import edu.umd.cs.findbugs.annotations.NonNull;

public class FileTransactionHandlerImpl implements FileTransactionHandler {
	private final FileStore store;

	FileTransactionHandlerImpl(FileStore store) {
		this.store = store; // validate
	}

	@NonNull
	public void handleFileCreate(@NonNull HandleContext<RecordBuilder> ctx, FileCreateTransactionBody tx) {
	}
}
