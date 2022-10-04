package com.hedera.hashgraph.token.record;

import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.TokenID;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link RecordBuilder} used when a token has been created.
 */
public interface CreateTokenRecordBuilder extends RecordBuilder {
    @NonNull
    CreateTokenRecordBuilder tokenAlias(@NonNull byte[] value);

    @NonNull
    CreateTokenRecordBuilder receiptCreatedTokenID(@NonNull TokenID tokenID);
}
