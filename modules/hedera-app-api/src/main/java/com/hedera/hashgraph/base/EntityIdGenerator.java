package com.hedera.hashgraph.base;

// Get's rolled back if needed
public interface EntityIdGenerator {
    long nextNum();
}
