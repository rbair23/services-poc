package com.hedera.hashgraph.app;

import com.hedera.hashgraph.app.state.StateRegistryImpl;
import com.hedera.hashgraph.base.Service;

public record ServicesContext<S extends Service> (S service, StateRegistryImpl stateRegistry) {
}
