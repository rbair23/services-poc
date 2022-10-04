package com.hedera.hashgraph.app.workflows.ingest;

import com.hedera.hashgraph.base.ThrottleAccumulator;
import com.hedera.hashgraph.hapi.model.Key;
import com.hedera.hashgraph.hapi.model.KeyList;
import com.hedera.hashgraph.hapi.model.SignatureMap;
import com.hedera.hashgraph.hapi.model.TransactionBody;
import com.hedera.hashgraph.hapi.model.base.SignedTransaction;
import com.hedera.hashgraph.token.entity.Account;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Encapsulates the workflow related to transaction ingestion. Given a Transaction,
 * parses, validates, and submits to the platform.
 *
 */
public class IngestCheckerImpl implements IngestChecker {
    private final ThrottleAccumulator throttleAccumulator;

    public IngestCheckerImpl(ThrottleAccumulator throttleAccumulator) {
        this.throttleAccumulator = Objects.requireNonNull(throttleAccumulator);
    }

    @Override
    public void checkSignedTransaction(SignedTransaction tx) {
        // TODO Throw if sigmap is null
    }

    @Override
    public void checkTransactionBody(TransactionBody txBody, Account account) {
        // TODO check the payer is specified and start time and other stuff.
    }

    @Override
    public void checkSignatures(ByteBuffer signedTransactionBytes, SignatureMap signatureMap, Key key) {
        // TODO implement. For now assume it is always good
    }


    @Override
    public void checkThrottles(TransactionBody.DataOneOfType type) {
//        if (throttleAccumulator.shouldThrottle(type.name(), 1)) {
//            // TODO Throw an exception because we were throttled!
//        }
    }
}
