package com.hedera.hashgraph.hapi;

import java.nio.ByteBuffer;

/**
 * Utility methods used by generated parsers/writers
 */
public class Utils {

    /**
     * Get a copy of content of bytebuffer as a byte[] without changing any of the buffer's internal state. Also works
     * with read only byte buffers.
     *
     * @param buffer byte buffer to copy data from
     * @return new byte[] containing data
     */
    public static byte[] readOnlyByteBufferToByteArray(ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.capacity()];
        buffer.get(0, bytes);
        return bytes;
    }
}
