package com.hedera.hashgraph.base.state;

import java.util.Optional;
import java.util.function.BiFunction;

public interface StateRegistryCallback<K, V>
        extends BiFunction<StateBuilder, Optional<State<K, V>>, State<K, V>> {
}
