package com.hedera.hashgraph.base.model;

public record TransactionResponse(ResponseCode nodeTransactionPrecheckCode, long cost) {
}
