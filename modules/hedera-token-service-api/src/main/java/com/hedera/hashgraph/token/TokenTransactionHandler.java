package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.base.TransactionHandler;
import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.token.*;
import com.hedera.hashgraph.token.record.CreateTokenRecordBuilder;
import com.hedera.hashgraph.token.record.TokenRecordBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link TransactionHandler} for methods defined in the protobuf "TokenService".
 */
public interface TokenTransactionHandler extends TransactionHandler {
    /**
     * Creates a new Token by submitting the transaction
     */
    void createToken(@NonNull HandleContext<CreateTokenRecordBuilder> ctx, @NonNull TokenCreateTransactionBody tx);

    /**
     * Updates the account by submitting the transaction
     */
    void updateToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenUpdateTransactionBody tx);

    /**
     * Mints an amount of the token to the defined treasury account
     */
    void mintToken(@NonNull HandleContext<TokenRecordBuilder> ctx, @NonNull TokenMintTransactionBody tx);

    /**
     * Burns an amount of the token from the defined treasury account
     */
    void burnToken(@NonNull HandleContext<TokenRecordBuilder> ctx, @NonNull TokenBurnTransactionBody tx);

    /**
     * Deletes a Token
     */
    void deleteToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenDeleteTransactionBody tx);

    /**
     * Wipes the provided amount of tokens from the specified Account ID
     */
    void wipeTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenWipeAccountTransactionBody tx);

    /**
     * Freezes the transfer of tokens to or from the specified Account ID
     */
    void freezeTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenFreezeAccountTransactionBody tx);

    /**
     * Unfreezes the transfer of tokens to or from the specified Account ID
     */
    void unfreezeTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenUnfreezeAccountTransactionBody tx);

    /**
     * Flags the provided Account ID as having gone through KYC
     */
    void grantKycToTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenGrantKycTransactionBody tx);

    /**
     * Removes the KYC flag of the provided Account ID
     */
    void revokeKycFromTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenRevokeKycTransactionBody tx);

    /**
     * Associates tokens to an account
     */
    void associateTokens(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenAssociateTransactionBody tx);

    /**
     * Dissociates tokens from an account
     */
    void dissociateTokens(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenDissociateTransactionBody tx);

    /**
     * Updates the custom fee schedule on a token
     */
    void updateTokenFeeSchedule(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenFeeScheduleUpdateTransactionBody tx);

    /**
     * Pause the token
     */
    void pauseToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenPauseTransactionBody tx);

    /**
     * Unpause the token
     */
    void unpauseToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenUnpauseTransactionBody tx);
}
