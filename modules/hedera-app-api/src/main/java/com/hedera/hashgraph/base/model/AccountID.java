package com.hedera.hashgraph.base.model;

// NOTE: Here we are storing shard and realm even though it is a waste of memory (!!) and,
// we are dealing with the "one-of" by passing both inputs rather than creating a super type and all that noise.
public record AccountID(long shardNum, long realmNum, long accountNum, byte[] alias) {
}
