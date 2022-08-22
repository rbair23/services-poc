package com.hedera.hashgraph.base;

import com.hedera.hashgraph.base.ChangeManager;
import com.hedera.hashgraph.base.FeeAccumulator;
import com.hedera.hashgraph.base.ThrottleAccumulator;

public record HandleContext(ChangeManager changeManager, ThrottleAccumulator throttleAccumulator, FeeAccumulator feeAccumulator) {
}
