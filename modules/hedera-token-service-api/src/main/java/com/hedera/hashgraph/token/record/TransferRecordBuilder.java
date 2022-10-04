package com.hedera.hashgraph.token.record;

import com.hedera.hashgraph.base.FeeAccumulator;
import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.AccountAmount;
import com.hedera.hashgraph.hapi.model.TransferList;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link RecordBuilder} with API for recording crypto transfers (HBAR and other tokens), and
 * for recording fees, and staking rewards. This class is only used when finalizing transfers,
 * in the {@link com.hedera.hashgraph.token.CryptoTransactionHandler#finalizeTransfers(FeeAccumulator, TransferRecordBuilder)}
 * method.
 */
public interface TransferRecordBuilder extends RecordBuilder {
    /**
     * The actual transaction fee charged, not the original transactionFee value from
     * TransactionBody
     */
    @NonNull TransferRecordBuilder transactionFee(long value);

    /**
     * All hbar transfers as a result of this transaction, such as fees, or transfers performed by
     * the transaction, or by a smart contract it calls, or by the creation of threshold records
     * that it triggers.
     */
    @NonNull TransferRecordBuilder transferList(@NonNull TransferList transferList);

    /**
     * List of accounts with the corresponding staking rewards paid as a result of a transaction.
     */
    @NonNull TransferRecordBuilder addPaidStakingRewards(@NonNull AccountAmount rewards);
}
