package com.hedera.hashgraph.base.model;

public record SignedTransaction(byte[] bodyBytes, SignatureMap sigMap) {
}
