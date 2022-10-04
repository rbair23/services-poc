package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.file.FileCreateTransactionBody;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface FileTransactionHandler extends TransactionHandler {
	void handleFileCreate(@NonNull HandleContext<RecordBuilder> ctx, @NonNull FileCreateTransactionBody tx);
}
