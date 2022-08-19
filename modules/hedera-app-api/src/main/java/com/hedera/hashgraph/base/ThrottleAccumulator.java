package com.hedera.hashgraph.base;

public interface ThrottleAccumulator {
    boolean shouldThrottle(String key, long incrementalAmount);
}
