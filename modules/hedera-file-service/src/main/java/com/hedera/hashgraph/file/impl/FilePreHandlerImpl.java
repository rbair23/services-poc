package com.hedera.hashgraph.file.impl;

import com.hedera.hashgraph.base.TransactionMetadata;
import com.hedera.hashgraph.file.FilePreHandler;
import com.hedera.hashgraph.hapi.model.file.FileCreateTransactionBody;

public class FilePreHandlerImpl implements FilePreHandler {
    @Override
    public TransactionMetadata preHandleFileCreate(FileCreateTransactionBody tx) {
        return null;
    }
}
