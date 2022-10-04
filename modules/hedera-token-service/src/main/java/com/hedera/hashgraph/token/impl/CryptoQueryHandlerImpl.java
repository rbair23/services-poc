package com.hedera.hashgraph.token.impl;

import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.hapi.model.base.TransactionGetReceiptQuery;
import com.hedera.hashgraph.hapi.model.base.TransactionGetRecordQuery;
import com.hedera.hashgraph.hapi.model.token.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.hapi.model.token.CryptoGetAccountRecordsQuery;
import com.hedera.hashgraph.hapi.model.token.GetAccountDetailsQuery;
import com.hedera.hashgraph.token.CryptoQueryHandler;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.impl.store.AccountStore;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

public class CryptoQueryHandlerImpl implements CryptoQueryHandler {
    private final AccountStore accountStore;

    CryptoQueryHandlerImpl(AccountStore accountStore) {
        this.accountStore = accountStore;
    }


    @Override
    public Optional<Account> getAccountById(@NonNull AccountID id) {
        return accountStore.getAccount(id);
    }

    @Override
    public void getAccountRecords(@NonNull CryptoGetAccountRecordsQuery query) {

    }

    @Override
    public void cryptoGetBalance(@NonNull CryptoGetAccountBalanceQuery query) {

    }

    @Override
    public void getAccountInfo(@NonNull GetAccountDetailsQuery query) {

    }

    @Override
    public void getTransactionReceipts(@NonNull TransactionGetReceiptQuery query) {

    }

    @Override
    public void getTxRecordByTxID(@NonNull TransactionGetRecordQuery query) {

    }
}
