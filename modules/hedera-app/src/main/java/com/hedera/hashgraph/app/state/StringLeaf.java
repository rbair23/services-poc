package com.hedera.hashgraph.app.state;

import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.common.merkle.MerkleLeaf;
import com.swirlds.common.merkle.impl.PartialMerkleLeaf;
import com.swirlds.common.utility.CommonUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;

/**
 * A {@link MerkleLeaf} containing a single String value. This is an immutable leaf -- once set,
 * the leaf value can never change. The maximum size for the leaf value is 128 bytes.
 */
public final class StringLeaf extends PartialMerkleLeaf implements MerkleLeaf {
    private static final long CLASS_ID = 99992323882382L;
    private static final int VERSION_0 = 0;
    private static final int CURRENT_VERSION = VERSION_0;
    private static final int MAX_LENGTH = 128;

    private String value = "";

    /**
     * @deprecated Used by the deserialization system only
     */
    @Deprecated(since = "1.0")
    public StringLeaf() {

    }

    /**
     * Create a new instance with the given value for the leaf.
     *
     * @param value The value cannot be null.
     */
    public StringLeaf(@Nonnull String value) {
        this.value = Objects.requireNonNull(value);
        final byte[] data = CommonUtils.getNormalisedStringBytes(value);
        if (data.length > MAX_LENGTH) {
            throw new IllegalArgumentException("The maximum *normalized* string length allowed is " + MAX_LENGTH);
        }
    }

    /**
     * Gets the value.
     * @return A non-null string.
     */
    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    public MerkleLeaf copy() {
        // I can return the same instance because the value is immutable
        return this;
    }

    @Override
    public long getClassId() {
        return CLASS_ID;
    }

    @Override
    public void deserialize(SerializableDataInputStream in, int i) throws IOException {
        this.value = in.readNormalisedString(MAX_LENGTH);
    }

    @Override
    public void serialize(SerializableDataOutputStream out) throws IOException {
        out.writeNormalisedString(this.value);
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }
}
