package com.hedera.hashgraph.base;

public record HandleContext(
        ChangeManager changeManager,
        ThrottleAccumulator throttleAccumulator,
        FeeAccumulator feeAccumulator) {
}
