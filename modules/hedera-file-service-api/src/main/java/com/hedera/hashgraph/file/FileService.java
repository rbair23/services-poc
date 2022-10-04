package com.hedera.hashgraph.file;

import com.hedera.hashgraph.base.Service;
import com.hedera.hashgraph.base.state.States;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface FileService extends Service {
    @Override
    @NonNull
    FileTransactionHandler createTransactionHandler(@NonNull States states);

    @NonNull
    @Override
    FileQueryHandler createQueryHandler(@NonNull States states);
}
