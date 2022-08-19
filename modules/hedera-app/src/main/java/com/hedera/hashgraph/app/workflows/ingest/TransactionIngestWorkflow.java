package com.hedera.hashgraph.app.workflows.ingest;

import com.hedera.hashgraph.hapi.model.KeyList;
import com.hedera.hashgraph.hapi.model.SignatureMap;
import com.hedera.hashgraph.hapi.model.TransactionBody;
import com.hedera.hashgraph.hapi.model.base.SignedTransaction;
import com.hedera.hashgraph.hapi.model.base.Transaction;
import com.hedera.hashgraph.hapi.proto.parsers.TransactionParser;

/**
 * Encapsulates the workflow related to transaction ingestion. Given a Transaction,
 * parses, validates, and submits to the platform.
 */
public class TransactionIngestWorkflow {
    // Or do we create some kind of context object and pass it to "run" that has the thread local state, so
    // we don't have to keep going back to threadlocal for everything? Ya, much, much safer.
    private final ThreadLocal<TransactionParser> parserThreadLocal = new ThreadLocal<>() {
        @Override
        protected TransactionParser initialValue() {
            return new TransactionParser();
        }
    };

    private Object accountService = null;

    public void run(IngestContext ctx, Transaction tx) {
        // 0. At this point, our marshaller has already parsed the transaction and thrown an
        //    error if there was some problem parsing the protobuf.
        // 1. Next, fire up a parser for parsing out the signed transaction object and throw
        //    an error if that failed to parse. Also throw an error if the sigmap is empty.
        final var signedTransaction = parseAndValidateSignedTransaction(tx.signedTransactionBytes());
        // 2. Time to parse the TransactionBody. Throw an error if it doesn't parse right.
        //    We use a custom TransactionBodyParser that skips the "data" field. We don't
        //    need it at this stage of processing, and so parsing it is a waste of time.
        //    Also check that the transaction ID is valid, and other fields. Ask the
        //    TokenService for account information to validate that the account has enough
        //    HBAR to pay the node & network fees. And verify the start transaction time is
        //    valid. Throw exceptions if things don't look good. Otherwise, on to step 3!
        final var txBody = parseAndValidateTransactionBody(signedTransaction.bodyBytes());
        // TODO Get an account entity object from the token service / account service.
        final var account = "";
        final var keyList = new KeyList(null); // TODO get from account obviously
        // 3. Now that we have the account information, it is time to validate signatures.
        checkSignatures(tx.signedTransactionBytes(), signedTransaction.sigMap(), keyList);
        // 4. If signatures all check out, then check the throttles. Ya, I'd like to check
        //    throttles way back on step 1, but we need to verify whether the account is
        //    a privileged account (less than 0.0.100) and if so skip throttles. Without
        //    a way to authenticate the payload any earlier than step 3, we have to do it now.
        // TODO THROTTLES
        // 5. If we get past the throttles, then it is finally time to submit to the platform
        //    for consensus. Way cool!
        // TODO SUBMIT TO CONSENSUS
        // 6. And if we made it all the way here without any kind of trauma, then we can create
        //    a nice response and send it back to the caller. Yippee.
        // TODO ANything to do here?
        System.out.println("I WAS CALLED!!");
    }

    private SignedTransaction parseAndValidateSignedTransaction(byte[] signedTransactionBytes) {
        // TODO Implement this
        return new SignedTransaction(new byte[0], null);
    }

    private TransactionBody parseAndValidateTransactionBody(byte[] bodyBytes) {
        // TODO Implement this
        return new TransactionBody(null, null, 1L, null, false, null, null);
    }

    private void checkSignatures(byte[] signedTransactionBytes, SignatureMap signatureMap, KeyList keyList) {
        // TODO Implement this
    }
}
