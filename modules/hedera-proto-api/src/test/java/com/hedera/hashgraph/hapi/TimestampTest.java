package com.hedera.hashgraph.hapi;

import com.hedera.hashgraph.hapi.model.base.Timestamp;
import com.hedera.hashgraph.hapi.writer.base.TimestampWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TimestampTest {

    @Test
    public void testWrite() {
        final Timestamp timestamp = new Timestamp(1,2);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        th
        try {
            TimestampWriter.write(timestamp,bout);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
