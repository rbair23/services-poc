package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.hapi.parsers.proto.TransactionBodyProtoParser;
import com.hedera.hashgraph.hapi.parsers.proto.base.SignedTransactionProtoParser;
import com.hedera.hashgraph.hapi.parsers.proto.base.TransactionProtoParser;

// Put the per-thread stuff here like parsers
record SessionContext(
        TransactionProtoParser txParser,
        SignedTransactionProtoParser signedParser,
        TransactionBodyProtoParser txBodyParser) {
}
