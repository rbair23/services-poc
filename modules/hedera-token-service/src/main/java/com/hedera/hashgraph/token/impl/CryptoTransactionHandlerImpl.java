package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.FeeAccumulator;
import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.*;
import com.hedera.hashgraph.hapi.model.token.*;
import com.hedera.hashgraph.hapi.schema.AccountIDSchema;
import com.hedera.hashgraph.token.CryptoTransactionHandler;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.impl.store.AccountStore;
import com.hedera.hashgraph.token.record.CreateAccountRecordBuilder;
import com.hedera.hashgraph.token.record.TokenTransferRecordBuilder;
import com.hedera.hashgraph.token.record.TransferRecordBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

// Business Logic
public class CryptoTransactionHandlerImpl implements CryptoTransactionHandler {
    private final AccountStore accountStore;

    CryptoTransactionHandlerImpl(AccountStore accountStore) {
        this.accountStore = accountStore;
    }

    @Override
    public void createGenesisAccount(@NonNull Account account) {
        accountStore.saveAccount(account);
    }

    @Override
    public void finalizeTransfers(@NonNull FeeAccumulator fees, @NonNull TransferRecordBuilder rb) {
        // Step 1: Given the fees, modify the different accounts involved (nodes, payer) with these fees

        // Step 2: Create the transfer list representing all those changes plus all the other changes to accounts
        //         that were modified during the transaction
        final var transferList = new TransferList(List.of(new AccountAmount(null, 4444, true)));
        // Step 3: Set the transfer list on the record builder
        rb.transactionFee(123);
        rb.transferList(transferList);
    }

    @Override
    public void createAccount(@NonNull HandleContext<CreateAccountRecordBuilder> ctx, @NonNull CryptoCreateTransactionBody tx) {
        // Make sure shardID is right
        if (tx.shardID() != null && tx.shardID().shardNum() != 0) {
            throw new IllegalArgumentException("The shard num was not correct");
        }

        // Make sure realmID is right
        if (tx.realmID() != null && tx.realmID().realmNum() != 0) {
            throw new IllegalArgumentException("The realm num was not correct");
        }

        // Any other validation? Key must be specified. Anything else?


        if (!ctx.throttleAccumulator().shouldThrottle(HederaFunctionality.CRYPTO_CREATE, 1)) {
            // Throw some exception because we have exceeded our throttle
            // TODO
            return;
        }

        // Create the account with all the data supplied in the transaction
        final var account = accountStore.createAccount(
                ctx.idGenerator().nextNum(),
                tx.key(),
                tx.initialBalance(),
                tx.receiverSigRequired(),
                tx.autoRenewPeriod().seconds(),
                tx.memo(),
                tx.maxAutomaticTokenAssociations(),
                1234, //tx.stakedId().,
                tx.declineReward());

        // Accumulate fees related to an account creation
        ctx.feeAccumulator().accumulate(HederaFunctionality.CRYPTO_CREATE);

        // Save the created account to the store (does not commit to the merkle tree)
        accountStore.saveAccount(account);

        // Save the info in the record builder
        final var accountID = new AccountID(0, 0, new OneOf<>(
                AccountID.AccountOneOfType.ACCOUNT_NUM, account.accountNumber()));
        ctx.recordBuilder().receiptCreatedAccountID(accountID);
    }

    @Override
    public void updateAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoUpdateTransactionBody tx) {

    }

    @Override
    public void cryptoTransfer(@NonNull HandleContext<TokenTransferRecordBuilder> ctx, @NonNull CryptoTransferTransactionBody tx) {

    }

    @Override
    public void cryptoDelete(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoDeleteTransactionBody tx) {

    }

    @Override
    public void approveAllowances(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoApproveAllowanceTransactionBody tx) {

    }

    @Override
    public void deleteAllowances(@NonNull HandleContext<RecordBuilder> ctx, @NonNull CryptoDeleteAllowanceTransactionBody tx) {

    }
}
