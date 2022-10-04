package com.hedera.hashgraph.app.state;

public interface HederaState {
    StatesImpl createStates(String serviceName);

    long getNextEntityId();
    void setNextEntityId(long nextEntityId);
}
