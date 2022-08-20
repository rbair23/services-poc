package com.hedera.hashgraph.base;

public interface FeeAccumulator {
	void addBpt(long amount);
	void addBpr(long amount);
	void addSbpr(long amount);
	void addVpt(long amount);
	void addGas(long amount);
	void addRbs(long amount);
	void addSbs(long amount);
	void addNetworkRbs(long amount);

	/* Provider-scoped usage estimates (pure functions of the total resource usage) */
	/* -- NETWORK & NODE -- */
	long getUniversalBpt();

	/* -- NETWORK -- */
	long getNetworkVpt();

	long getNetworkRbh();

	/* -- NODE -- */
	long getNodeBpr();

	long getNodeSbpr();

	long getNodeVpt();

	/* -- SERVICE -- */
	long getServiceRbh();

	long getServiceSbh();

	void setNumPayerKeys(long numPayerKeys);
}
