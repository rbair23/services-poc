package com.hedera.hashgraph.app.throttle;

import com.hedera.hashgraph.base.ThrottleAccumulator;

public class ThrottleAccumulatorImpl implements ThrottleAccumulator {
    @Override
    public boolean shouldThrottle(String key, long incrementalAmount) {
        // TODO Implement
        return false;
    }
}
