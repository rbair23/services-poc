package com.hedera.hashgraph.file.impl;

import com.hedera.hashgraph.base.ChangeManager;
import com.hedera.hashgraph.file.FileTransactionHandler;
import com.hedera.hashgraph.file.impl.store.FileStore;
import com.hedera.hashgraph.hapi.model.TransactionRecord;
import com.hedera.hashgraph.hapi.model.file.FileCreateTransactionBody;

// todo how to do rollback and commit? especially across modules
public class FileTransactionHandlerImpl implements FileTransactionHandler {
	private final FileStore store;

	FileTransactionHandlerImpl(FileStore store) {
		this.store = store; // validate
	}

	public TransactionRecord handleFileCreate(ChangeManager cm, FileCreateTransactionBody tx) {
		// Actually create the file and return the record or something you bozo
		// todo do other types of validation here that make sense
		final var fileInfo = store.createFile(tx); // doesn't modify the merkel tree. Gotta save too.
		cm.addChange(() -> store.saveFile(fileInfo));
		// deal with problems. Create a record.

		return null;
	}

//	public TransactionRecord updateFile(FileUpdateTransactionBody tx) {
//		return null;
//	}

//	public TransactionRecord updateFile(FileDeleteTransactionBody tx) {
//		return null;
//	}

//	public TransactionRecord updateFile(FileAppendTransactionBody tx) {
//		return null;
//	}

//	/**
//	 * Retrieves the file contents
//	 */
//	rpc getFileContent (Query) returns (Response);
//
//	/**
//	 * Retrieves the file information
//	 */
//	rpc getFileInfo (Query) returns (Response);
//
//	/**
//	 * Deletes a file if the submitting account has network admin privileges
//	 */
//	rpc systemDelete (Transaction) returns (TransactionResponse);
//
//	/**
//	 * Undeletes a file if the submitting account has network admin privileges
//	 */
//	rpc systemUndelete (Transaction) returns (TransactionResponse);
}
