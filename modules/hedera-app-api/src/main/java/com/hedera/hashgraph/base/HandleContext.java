package com.hedera.hashgraph.base;

import com.hedera.hashgraph.base.record.RecordBuilder;

public record HandleContext<B extends RecordBuilder>(
        EntityIdGenerator idGenerator,
        ThrottleAccumulator throttleAccumulator,
        FeeAccumulator feeAccumulator,
        B recordBuilder) {
}
