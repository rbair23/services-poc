package com.hedera.hashgraph.hapi.schema;

import com.hedera.hashgraph.protoparse.FieldDefinition;
import com.hedera.hashgraph.protoparse.FieldType;

public class TransactionSchema {
    public static final FieldDefinition SIGNED_TRANSACTION_BYTES = new FieldDefinition("signedTransactionBytes", FieldType.BYTES, false, 5);

    private TransactionSchema() {

    }
}
