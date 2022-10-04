package com.hedera.hashgraph.app.state;

import com.swirlds.virtualmap.VirtualKey;
import com.swirlds.virtualmap.VirtualMap;
import com.swirlds.virtualmap.VirtualValue;

import javax.annotation.Nonnull;
import java.util.Objects;

public class OnDiskStateImpl<K extends VirtualKey<? super K>, V extends VirtualValue> extends StateBase<K, V> {
    private final VirtualMap<K, V> merkle;

    public OnDiskStateImpl(@Nonnull String stateKey, @Nonnull VirtualMap<K, V> merkle) {
        super(stateKey);
        this.merkle = Objects.requireNonNull(merkle);
    }

    @Nonnull protected VirtualMap<K, V> getMerkle() {
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
