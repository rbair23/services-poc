package com.hedera.hashgraph.app.state;

import com.swirlds.common.merkle.MerkleNode;
import com.swirlds.common.merkle.utility.Keyed;
import com.swirlds.merkle.map.MerkleMap;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * An implementation of {@link com.hedera.hashgraph.base.state.State} backed by a
 * {@link MerkleMap}, resulting in a state that is stored in memory.
 *
 * @param <K> The type of key for the state
 * @param <V> The type of value for the state
 */
public class InMemoryStateImpl<K, V extends MerkleNode & Keyed<K>> extends StateBase<K, V> {
    private final MerkleMap<K, V> merkle;

    /**
     * Creates a new instance.
     *
     * @param stateKey The state key must be specified (not null or empty) and must be less than
     *                 128 bytes after having been normalized (i.e. including multi-byte chars)
     */
    public InMemoryStateImpl(@Nonnull String stateKey) {
        super(stateKey);
        this.merkle = new MerkleMap<>();
    }

    /**
     * Creates a new instance.
     *
     * @param stateKey The state key must be specified (not null or empty) and must be less than
     *                 128 bytes after having been normalized (i.e. including multi-byte chars)
     * @param merkleMap The map to use. Cannot be null.
     */
    public InMemoryStateImpl(@Nonnull String stateKey, @Nonnull MerkleMap<K, V> merkleMap) {
        super(stateKey);
        this.merkle = Objects.requireNonNull(merkleMap);
    }

    @Nonnull protected MerkleMap<K, V> getMerkle() {
        return merkle;
    }

    @Override
    protected V read(K key) {
        return this.merkle.get(key);
    }

    @Override
    protected V readForModify(K key) {
        return this.merkle.getForModify(key);
    }

    @Override
    protected void write(K key, V value) {
        this.merkle.put(key, value);
    }

    @Override
    protected void delete(K key) {
        this.merkle.remove(key);
    }
}
