package com.hedera.hashgraph.app.workflows.ingest;

public class IngestContext {
    // Put signed transaction parser here, and other thread-local context state. I want EVERYTHING
    // outside the grpc package to be able to ignore multiple concurrent threads as much as possible...
    // I really want to have all the different parsers available here. Maybe some generic way to get any
    // parser or builder...? Or maybe not. Maybe just for ingest which only has a few parser it needs.
    // Ya, probably that.
}
