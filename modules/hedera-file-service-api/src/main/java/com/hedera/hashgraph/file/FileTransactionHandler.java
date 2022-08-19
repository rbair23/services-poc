package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.ChangeManager;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.hapi.model.TransactionRecord;
import com.hedera.hashgraph.hapi.model.file.FileCreateTransactionBody;

// todo how to do rollback and commit? especially across modules
public interface FileTransactionHandler extends TransactionHandler {
	TransactionRecord createFile(ChangeManager cm, FileCreateTransactionBody tx);
}
