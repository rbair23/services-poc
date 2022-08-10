package com.hedera.hashgraph.base;

public interface Service {
	Endpoint getEndpoint();
	TransactionHandler getTransactionHandler();
	QueryHandler getQueryHandler();
}
