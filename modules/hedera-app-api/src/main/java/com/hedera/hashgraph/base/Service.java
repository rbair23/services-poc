package com.hedera.hashgraph.base;

public interface Service {
	TransactionHandler getTransactionHandler();
	QueryHandler getQueryHandler();
}
