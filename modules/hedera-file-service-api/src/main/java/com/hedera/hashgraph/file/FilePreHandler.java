package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.TransactionMetadata;
import com.hedera.hashgraph.hapi.model.file.FileCreateTransactionBody;

public interface FilePreHandler {
    TransactionMetadata preHandleFileCreate(FileCreateTransactionBody tx);
}
