package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.Service;

public interface FileService extends Service {
    FilePreHandler preHandler();
    FileTransactionHandler transactionHandler();
}
