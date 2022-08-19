package com.hedera.hashgraph.hapi.proto.parsers;

import com.hedera.hashgraph.hapi.model.base.Transaction;
import com.hedera.hashgraph.protoparse.FieldDefinition;
import com.hedera.hashgraph.protoparse.MalformedProtobufException;
import com.hedera.hashgraph.protoparse.ProtoParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.hedera.hashgraph.hapi.schema.TransactionSchema.SIGNED_TRANSACTION_BYTES;

// A very temporary class until Jasper's generator is merged
public class TransactionParser extends ProtoParser {
    private byte[] signedTransactionBytes = new byte[0];

    public Transaction parse(InputStream protobuf) throws IOException, MalformedProtobufException {
        signedTransactionBytes = new byte[0];
        super.start(protobuf);
        return new Transaction(null, null, null, null, signedTransactionBytes);
    }

    @Override
    protected FieldDefinition getFieldDefinition(final int fieldNumber) {
        return switch (fieldNumber) {
            case 1 -> SIGNED_TRANSACTION_BYTES;
            default -> null;
        };
    }

    @Override
    public void bytesField(final int fieldNum, final byte[] value) {
        if (fieldNum != SIGNED_TRANSACTION_BYTES.number()) {
            throw new AssertionError("Unknown field number " + fieldNum);
        }

        this.signedTransactionBytes = value;
    }
}
