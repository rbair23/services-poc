package com.hedera.hashgraph.app.record;

import com.hedera.hashgraph.base.record.RecordBuilder;
import com.hedera.hashgraph.hapi.model.*;
import com.hedera.hashgraph.token.record.*;

import java.util.List;

public class RecordBuilderImpl implements RecordBuilder, TransferRecordBuilder, CreateAccountRecordBuilder, TokenTransferRecordBuilder, CreateTokenRecordBuilder, TokenRecordBuilder {
    @Override
    public CreateAccountRecordBuilder accountAlias(byte[] value) {
        return this;
    }

    @Override
    public CreateTokenRecordBuilder tokenAlias(byte[] value) {
        return this;
    }

    @Override
    public CreateTokenRecordBuilder receiptCreatedTokenID(TokenID tokenID) {
        return this;
    }

    @Override
    public CreateAccountRecordBuilder receiptCreatedAccountID(AccountID accountID) {
        return this;
    }

    @Override
    public TokenRecordBuilder receiptNewTotalSupply(long value) {
        return this;
    }

    @Override
    public TokenRecordBuilder receiptTotalIssued(long value) {
        return this;
    }

    @Override
    public TokenRecordBuilder receiptSerialNumbers(long value) {
        return this;
    }

    @Override
    public TokenTransferRecordBuilder tokenTransferList(TokenTransferList value) {
        return this;
    }

    @Override
    public TokenTransferRecordBuilder assessedCustomFees(List<AssessedCustomFee> values) {
        return this;
    }

    @Override
    public TokenTransferRecordBuilder automaticTokenAssociations(List<TokenAssociation> values) {
        return this;
    }

    @Override
    public TransferRecordBuilder transactionFee(long value) {
        return this;
    }

    @Override
    public TransferRecordBuilder transferList(TransferList transferList) {
        return this;
    }

    @Override
    public TransferRecordBuilder addPaidStakingRewards(AccountAmount rewards) {
        return this;
    }

    public TransactionRecord build() {
        return null;
    }
}
