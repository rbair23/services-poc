package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.FeeAccumulator;
import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.token.*;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.record.CreateAccountRecordBuilder;
import com.hedera.hashgraph.token.record.TokenTransferRecordBuilder;
import com.hedera.hashgraph.token.record.TransferRecordBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A definition of an interface for handling all transactions defined in the protobuf "CryptoService",
 * along with some functions required by core application workflows for handling transaction processing.
 */
public interface CryptoTransactionHandler extends TransactionHandler {
    /**
     * Special method for creating a "genesis" account. There are no fees, records, throttles, or other
     * impediments or side effects. It simply creates the given account, assuming it is all correct.
     *
     * @param account The account to create
     */
    void createGenesisAccount(@NonNull Account account);

    /**
     * Called by the application query and transaction processing workflows to finalize all fees and payments,
     * and construct the final set of transfers both in state and in the supplied {@link TransferRecordBuilder}.
     * The implementation of this method takes the given fees and uses them to determine any changes to accounts
     * necessary to pay for those fees. It also creates the transfer list in the record builder. It will also
     * compute any pending rewards for any account involved in the transactions and apply those rewards, after
     * having handled all fees (TODO or is it before?).
     *
     * @param fees The fees to apply
     * @param rb The record builder to apply state to
     */
    void finalizeTransfers(@NonNull FeeAccumulator fees, @NonNull TransferRecordBuilder rb);

    /**
     * Creates a new account by submitting the transaction
     */
    void createAccount(@NonNull HandleContext<CreateAccountRecordBuilder> ctx, @NonNull CryptoCreateTransactionBody tx);

    /**
     * Updates an account by submitting the transaction
     */
    void updateAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoUpdateTransactionBody tx);

    /**
     * Initiates a transfer by submitting the transaction
     */
    void cryptoTransfer(@NonNull HandleContext<TokenTransferRecordBuilder> ctx, @NonNull CryptoTransferTransactionBody tx);

    /**
     * Deletes and account by submitting the transaction
     */
    void cryptoDelete(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoDeleteTransactionBody tx);

    /**
     * Adds one or more approved allowances for spenders to transfer the paying account's hbar or tokens.
     */
    void approveAllowances(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoApproveAllowanceTransactionBody tx);

    /**
     * Deletes one or more of the specific approved NFT serial numbers on an owner account.
     */
    void deleteAllowances(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoDeleteAllowanceTransactionBody tx);
}
