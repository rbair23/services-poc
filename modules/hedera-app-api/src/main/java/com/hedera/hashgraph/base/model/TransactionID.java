package com.hedera.hashgraph.base.model;

public record TransactionID(Timestamp transactionValidStart, AccountID accountID, boolean scheduled, int nonce) {
}
