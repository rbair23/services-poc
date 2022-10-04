package com.hedera.hashgraph.base;

import com.hedera.hashgraph.hapi.model.HederaFunctionality;

public interface FeeAccumulator {
	void accumulate(HederaFunctionality feature);
}
