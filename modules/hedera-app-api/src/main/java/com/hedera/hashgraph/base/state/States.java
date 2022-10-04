package com.hedera.hashgraph.base.state;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A set of {@link State} used by a service. Any service may have one or more {@link State}s,
 * and this set is made available to the service during transaction handling by the application.
 */
public interface States {
    /**
     * Gets the {@link State} associated with the given stateKey. If the state cannot be found,
     * an exception is thrown. This should **never** happen in an application, and represents a
     * fatal bug.
     *
     * @param stateKey The key used for looking up state
     * @return The State for that key. This will never be null.
     * @param <K> The key type in the State.
     * @param <V> The value type in the State.
     * @throws IllegalArgumentException if the state cannot be found, or stateKey is null.
     */
    @NonNull
    <K, V> State<K, V> get(@NonNull String stateKey);
}
