package com.hedera.hashgraph.token.record;

import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.AssessedCustomFee;
import com.hedera.hashgraph.hapi.model.TokenAssociation;
import com.hedera.hashgraph.hapi.model.TokenTransferList;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

/**
 * A {@link RecordBuilder} used for recording token transfer information, suitable for all transactions
 * involving token transfers.
 */
public interface TokenTransferRecordBuilder extends RecordBuilder {
    @NonNull
    TokenTransferRecordBuilder tokenTransferList(@NonNull TokenTransferList value);

    @NonNull
    TokenTransferRecordBuilder assessedCustomFees(@NonNull List<AssessedCustomFee> values);

    @NonNull
    TokenTransferRecordBuilder automaticTokenAssociations(@NonNull List<TokenAssociation> values);
}
