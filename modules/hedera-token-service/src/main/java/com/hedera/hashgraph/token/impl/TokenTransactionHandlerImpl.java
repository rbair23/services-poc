package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.base.HandleContext;
import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.token.*;
import com.hedera.hashgraph.token.TokenTransactionHandler;
import com.hedera.hashgraph.token.impl.store.TokenStore;
import com.hedera.hashgraph.token.record.CreateTokenRecordBuilder;
import com.hedera.hashgraph.token.record.TokenRecordBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

public class TokenTransactionHandlerImpl implements TokenTransactionHandler {
    private final TokenStore store;

    public TokenTransactionHandlerImpl(TokenStore store) {
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public void createToken(@NonNull HandleContext<CreateTokenRecordBuilder> ctx, @NonNull TokenCreateTransactionBody tx) {

    }

    @Override
    public void updateToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenUpdateTransactionBody tx) {

    }

    @Override
    public void mintToken(@NonNull HandleContext<TokenRecordBuilder> ctx, @NonNull TokenMintTransactionBody tx) {

    }

    @Override
    public void burnToken(@NonNull HandleContext<TokenRecordBuilder> ctx, @NonNull TokenBurnTransactionBody tx) {

    }

    @Override
    public void deleteToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenDeleteTransactionBody tx) {

    }

    @Override
    public void wipeTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenWipeAccountTransactionBody tx) {

    }

    @Override
    public void freezeTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenFreezeAccountTransactionBody tx) {

    }

    @Override
    public void unfreezeTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenUnfreezeAccountTransactionBody tx) {

    }

    @Override
    public void grantKycToTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenGrantKycTransactionBody tx) {

    }

    @Override
    public void revokeKycFromTokenAccount(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenRevokeKycTransactionBody tx) {

    }

    @Override
    public void associateTokens(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenAssociateTransactionBody tx) {

    }

    @Override
    public void dissociateTokens(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenDissociateTransactionBody tx) {

    }

    @Override
    public void updateTokenFeeSchedule(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenFeeScheduleUpdateTransactionBody tx) {

    }

    @Override
    public void pauseToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenPauseTransactionBody tx) {

    }

    @Override
    public void unpauseToken(@NonNull HandleContext<RecordBuilder> ctx, @NonNull TokenUnpauseTransactionBody tx) {

    }
}
