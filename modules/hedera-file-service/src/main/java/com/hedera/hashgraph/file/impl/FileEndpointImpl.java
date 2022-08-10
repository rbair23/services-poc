package com.hedera.hashgraph.file.impl;

import com.hedera.hashgraph.base.ChangeManager;
import com.hedera.hashgraph.base.model.ResponseCode;
import com.hedera.hashgraph.base.model.TransactionBody;
import com.hedera.hashgraph.file.FileEndpoint;
import com.hedera.hashgraph.file.impl.serde.FileCreateTransactionBodyParser;

public class FileEndpointImpl implements FileEndpoint {
	private final FileTransactionHandlerImpl txHandler;

	FileEndpointImpl(FileTransactionHandlerImpl txHandler) {
		this.txHandler = txHandler; // todo validation
	}

	// I'm responsible for doing my own dang pre-check here.
	// If the response is "OK", then the app code will end up submitting it for consensus.
	public ResponseCode handleFileCreateTransaction(TransactionBody body) {
		final var cm = new ChangeManager(); // Could make this reusable by just having a reset method or something
		final var tx = FileCreateTransactionBodyParser.parse(body.data().getBytes());
		// TODO perform validation on the tx before passing on to the tx handler
		final var rec = txHandler.createFile(cm, tx);
		// If rec is failed, it might have an impact on what we should return here
		// If it worked, then:
		cm.commit();
		return null;
	}
}
