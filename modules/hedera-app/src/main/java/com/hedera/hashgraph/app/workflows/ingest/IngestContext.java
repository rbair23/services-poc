package com.hedera.hashgraph.app.workflows.ingest;

import com.hedera.hashgraph.hapi.proto.parsers.TransactionParser;

public class IngestContext {
    // Put signed transaction parser here, and other thread-local context state. I want EVERYTHING
    // outside the grpc package to be able to ignore multiple concurrent threads as much as possible...
}
