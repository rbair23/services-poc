package com.hedera.hashgraph.token.record;

import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.AccountID;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link RecordBuilder} that records information about new accounts that are created.
 */
public interface CreateAccountRecordBuilder extends RecordBuilder {
    /**
     * In the record of a CryptoCreate transaction triggered by a user transaction with a
     * (previously unused) alias, the new account's alias.
     */
    @NonNull CreateAccountRecordBuilder accountAlias(@NonNull byte[] value);

    /**
     * In the receipt of a CryptoCreate, the id of the newly created account
     */
    @NonNull CreateAccountRecordBuilder receiptCreatedAccountID(@NonNull AccountID accountID);
}
