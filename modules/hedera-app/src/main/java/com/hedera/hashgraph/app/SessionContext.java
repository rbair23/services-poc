package com.hedera.hashgraph.app;

import com.hedera.hashgraph.hapi.parser.QueryProtoParser;
import com.hedera.hashgraph.hapi.parser.TransactionBodyProtoParser;
import com.hedera.hashgraph.hapi.parser.base.SignedTransactionProtoParser;
import com.hedera.hashgraph.hapi.parser.base.TransactionProtoParser;

// Put the per-thread stuff here like parsers
public record SessionContext(
        QueryProtoParser queryParser,
        TransactionProtoParser txParser,
        SignedTransactionProtoParser signedParser,
        TransactionBodyProtoParser txBodyParser) {
}
