package com.hedera.hashgraph.app.state;

import com.hedera.hashgraph.base.state.State;
import com.hedera.hashgraph.base.state.StateBuilder;
import com.swirlds.common.merkle.MerkleNode;
import com.swirlds.common.merkle.utility.Keyed;
import com.swirlds.jasperdb.JasperDbBuilder;
import com.swirlds.jasperdb.VirtualLeafRecordSerializer;
import com.swirlds.jasperdb.files.hashmap.KeySerializer;
import com.swirlds.merkle.map.MerkleMap;
import com.swirlds.virtualmap.VirtualKey;
import com.swirlds.virtualmap.VirtualMap;
import com.swirlds.virtualmap.VirtualValue;

import java.util.Objects;

/**
 * An implementation of {@link StateBuilder} for constructing {@link InMemoryStateImpl} or
 * {@link OnDiskStateImpl} depend on whether the user is asking for in memory or on disk state.
 */
public final class StateBuilderImpl implements StateBuilder {
    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V extends MerkleNode & Keyed<K>> InMemoryBuilder<K, V> inMemory(String stateKey) {
        return new InMemoryBuilderImpl<K, V>(stateKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K extends VirtualKey<? super K>, V extends VirtualValue> OnDiskBuilder<K, V> onDisk(String stateKey, String label) {
        return new OnDiskBuilderImpl<>(stateKey, label);
    }

    /**
     * An implementation for {@link com.hedera.hashgraph.base.state.StateBuilder.InMemoryBuilder}.
     *
     * @param <K> The key type
     * @param <V> The value type
     */
    private static final class InMemoryBuilderImpl<K, V extends MerkleNode & Keyed<K>>
            implements InMemoryBuilder<K, V> {
        private final String stateKey;

        InMemoryBuilderImpl(String stateKey) {
            this.stateKey = Objects.requireNonNull(stateKey);
        }

        @Override
        public State<K, V> build() {
            return new InMemoryStateImpl<>(stateKey);
        }
    }

    /**
     * An implementation of {@link com.hedera.hashgraph.base.state.StateBuilder.OnDiskBuilder}.
     *
     * @param <K> The key type
     * @param <V> The value type
     */
    private static final class OnDiskBuilderImpl<K extends VirtualKey<? super K>, V extends VirtualValue>
            implements OnDiskBuilder<K, V> {
        private final String stateKey;
        private final String label;
        private final JasperDbBuilder<K, V> builder;

        OnDiskBuilderImpl(String stateKey, String label) {
            this.stateKey = Objects.requireNonNull(stateKey);
            this.label = Objects.requireNonNull(label);
            this.builder = new JasperDbBuilder<>();
            // TODO other serializers and such here such as the internal serializer
        }

        @Override
        public OnDiskBuilder<K, V> keySerializer(KeySerializer<K> serializer) {
            builder.keySerializer(serializer);
            return this;
        }

        @Override
        public OnDiskBuilder<K, V> valueSerializer(VirtualLeafRecordSerializer<K, V> leafRecordSerializer) {
            builder.virtualLeafRecordSerializer(leafRecordSerializer);
            return this;
        }

        @Override
        public State<K, V> build() {
            return new OnDiskStateImpl<>(stateKey, new VirtualMap<>(label, builder));
        }
    }
}
