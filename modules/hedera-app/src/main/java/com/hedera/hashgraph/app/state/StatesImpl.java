package com.hedera.hashgraph.app.state;

import com.hedera.hashgraph.base.state.State;
import com.hedera.hashgraph.base.state.States;
import com.swirlds.merkle.map.MerkleMap;
import com.swirlds.virtualmap.VirtualMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class StatesImpl implements States {
    private final ServiceStateNode serviceRoot;
    private final Map<String, StateBase> states = new HashMap<>();

    public StatesImpl(ServiceStateNode serviceRoot) {
        this.serviceRoot = Objects.requireNonNull(serviceRoot);
    }

    @Override
    @Nonnull
    public <K, V> State<K, V> get(@Nonnull String stateKey) {
        final var state = states.computeIfAbsent(stateKey, (key) -> {
            final Optional<State<K, V>> opt = findExistingState(stateKey);
            return (StateBase) opt.orElse(null);
        });

        if (state != null) {
            //noinspection unchecked
            return (State<K, V>) state;
        } else {
            throw new IllegalArgumentException("stateKey not found");
        }
    }

    /**
     * Looks for and retrieves existing state for the given state key. The returned optional is empty
     * if no such thing could be found.
     *
     * @param stateKey The key of the state to look for.
     * @return A non-null {@link Optional} of the found state, or empty if it wasn't found.
     * @param <K> The key type
     * @param <V> The value type
     */
    @Nonnull
    public <K, V> Optional<State<K, V>> findExistingState(@Nonnull String stateKey) {
        final var merkleNode = serviceRoot.find(stateKey);
        assert merkleNode == null || merkleNode instanceof MerkleMap || merkleNode instanceof VirtualMap<?,?>;
        final var existingState = merkleNode == null
                ? null
                : merkleNode instanceof MerkleMap
                ? new InMemoryStateImpl(stateKey, (MerkleMap) merkleNode)
                : new OnDiskStateImpl(stateKey, (VirtualMap)merkleNode);
        return existingState == null ? Optional.empty() : Optional.of(existingState);
    }

    public void commit() {
        states.forEach((k, v) -> {
            if (v != null) {
                v.commit();
            }
        });
    }
}
