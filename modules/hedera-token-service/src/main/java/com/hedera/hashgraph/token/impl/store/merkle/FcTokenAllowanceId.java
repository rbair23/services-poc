package com.hedera.hashgraph.token.impl.store.merkle;

import com.swirlds.common.io.SelfSerializable;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;

import java.io.IOException;
import java.util.Objects;

public class FcTokenAllowanceId implements SelfSerializable, Comparable<FcTokenAllowanceId> {
    static final int RELEASE_023X_VERSION = 1;
    static final int CURRENT_VERSION = RELEASE_023X_VERSION;
    static final long RUNTIME_CONSTRUCTABLE_ID = 0xf55baa544950f139L;

    private long tokenNum;
    private long spenderNum;

    public FcTokenAllowanceId() {
        /* RuntimeConstructable */
    }

    public FcTokenAllowanceId(final long tokenNum, final long spenderNum) {
        this.tokenNum = tokenNum;
        this.spenderNum = spenderNum;
    }

    public static FcTokenAllowanceId from(final long tokenId, final long accountId) {
        return new FcTokenAllowanceId(tokenId, accountId);
    }

    @Override
    public void deserialize(final SerializableDataInputStream din, final int i) throws IOException {
        tokenNum = din.readLong();
        spenderNum = din.readLong();
    }

    @Override
    public void serialize(final SerializableDataOutputStream dos) throws IOException {
        dos.writeLong(tokenNum);
        dos.writeLong(spenderNum);
    }

    @Override
    public long getClassId() {
        return RUNTIME_CONSTRUCTABLE_ID;
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(FcTokenAllowanceId.class)) {
            return false;
        }

        final var that = (FcTokenAllowanceId) obj;
        return this.tokenNum == that.tokenNum && this.spenderNum == that.spenderNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenNum, spenderNum);
    }

    @Override
    public String toString() {
        return "[" + tokenNum + ", " + spenderNum + "]";
    }

    public long getTokenNum() {
        return tokenNum;
    }

    public long getSpenderNum() {
        return spenderNum;
    }

    @Override
    public int compareTo(final FcTokenAllowanceId that) {
        final var first = Long.compare(tokenNum, that.tokenNum);
        if (first != 0) {
            return first;
        }
        return Long.compare(spenderNum, that.spenderNum);
    }
}
