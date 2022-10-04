package com.hedera.hashgraph.app.workflows.ingest;

import com.hedera.hashgraph.hapi.model.Key;
import com.hedera.hashgraph.hapi.model.KeyList;
import com.hedera.hashgraph.hapi.model.SignatureMap;
import com.hedera.hashgraph.hapi.model.TransactionBody;
import com.hedera.hashgraph.hapi.model.base.SignedTransaction;
import com.hedera.hashgraph.token.entity.Account;

import java.nio.ByteBuffer;

public interface IngestChecker {
    void checkSignedTransaction(SignedTransaction tx) throws PreCheckException;
    void checkTransactionBody(TransactionBody txBody, Account account) throws PreCheckException;
    void checkSignatures(ByteBuffer signedTransactionBytes, SignatureMap signatureMap, Key key) throws PreCheckException;
    void checkThrottles(TransactionBody.DataOneOfType type) throws ThrottleException;
}
