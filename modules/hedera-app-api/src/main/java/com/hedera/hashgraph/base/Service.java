package com.hedera.hashgraph.base;

import com.hedera.hashgraph.base.state.States;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface Service {
    @NonNull TransactionHandler createTransactionHandler(@NonNull States states);
    @NonNull QueryHandler createQueryHandler(@NonNull States states);
}
