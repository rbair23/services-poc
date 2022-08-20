package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.app.workflows.ingest.IngestChecker;
import com.hedera.hashgraph.app.workflows.ingest.PreCheckException;
import com.hedera.hashgraph.app.workflows.ingest.ThrottleException;
import com.hedera.hashgraph.hapi.model.TransactionBody;
import com.hedera.hashgraph.hapi.model.TransactionID;
import com.hedera.hashgraph.hapi.model.TransactionResponse;
import com.hedera.hashgraph.hapi.model.base.ResponseCodeEnum;
import com.hedera.hashgraph.hapi.model.base.SignedTransaction;
import com.hedera.hashgraph.hapi.model.base.Transaction;
import com.hedera.hashgraph.protoparse.MalformedProtobufException;
import com.hedera.hashgraph.token.AccountService;
import com.swirlds.common.system.Platform;

import java.util.Objects;

/**
 * An implementation of the ingestion pipeline. This class is threadsafe, a single instance of it
 * can be used to execute concurrent transaction ingestion. This implementation is specific to ingestion
 * of protobuf. Common validation logic is delegated to the {@link IngestChecker}. The pipeline steps are
 * roughly as follows (see architecture documentation for more info):
 *
 * <ol>
 *     <li>Parse the {@link Transaction} object from the txBytes (raw protobuf bytes sent from the client). The
 *     precondition is that the txBytes must not exceed the configured maximum before this method is called (we
 *     do not validate that here). If the protobuf is invalid and does not parse into a {@code Transaction}
 *     object, then a {@link MalformedProtobufException} will be thrown.</li>
 *     <li>Parse the {@link SignedTransaction} from the {@code Transaction}'s {@code signedTransactionBytes}.
 *     Throw a {@link MalformedProtobufException} if the protobuf cannot be parsed. Throw a {@link PreCheckException}
 *     if the signature map is missing or empty.</li>
 *     <li>Parse the {@link TransactionBody} from the {@code SignedTransaction}'s {@code bodyBytes}. Throw the
 *     expected exceptions in case protobuf parsing fails or the pre-checks are invalid.</li>
 *     <li>Check the signature map to verify the presence of the signature for the payer in the {@link TransactionID}
 *     and verify the signature matches. Otherwise, throw a {@link PreCheckException}.</li>
 *     <li>Check the throttles. If they are exceeded, throw a {@link ThrottleException}.</li>
 *     <li>If all checks passed, then submit the transaction to the platform. If the platform does not accept
 *     the transaction, it is due to backpressure being applied, so throw a {@link BackPressureException}.</li>
 *     <li>Create and return protobuf bytes for a {@link TransactionResponse}.</li>
 *
 * </ol>
 */
final class GrpcIngestPipeline {
    /**
     * Used to check validity of different things at different steps.
     */
    private final IngestChecker checker;

    /**
     * Used for validating the payer account exists and sigs match and has sufficient
     * balance to pay network and node fees.
     */
    private final AccountService accountService;

    /**
     * Used for submitting transactions.
     */
    private final Platform platform;

    /**
     * Create a new ingestion pipeline. A single pipeline can be used by as many threads as you like.
     *
     * @param platform The platform cannot be null. We submit transactions to this platform.
     * @param accountService The account service to query for latest-signed-state information on the account existence,
     *                       keys, and balance. Cannot be null.
     * @param ingestChecker Implements all the non-protobuf related semantic checks for ingestion. Cannot be null.
     */
    GrpcIngestPipeline(Platform platform, AccountService accountService, IngestChecker ingestChecker) {
        this.platform = Objects.requireNonNull(platform);
        this.accountService = Objects.requireNonNull(accountService);
        this.checker = Objects.requireNonNull(ingestChecker);
    }

    /**
     * Called to handle a single transaction during the ingestion flow. The call terminates in
     * a {@link TransactionResponse} being returned to the client (for both successful and unsuccessful calls).
     * There are no unhandled exceptions (even Throwable is handled).
     *
     * @param session The per-request {@link SessionContext}.
     * @param txBytes The raw protobuf transaction bytes. Must be a transaction object.
     * @return The {@link TransactionResponse} as a protobuf byte[].
     */
    byte[] handleTransaction(SessionContext session, byte[] txBytes) {
        try {
            // 0. Parse the transaction object from the txBytes (protobuf)
            final var tx = session.txParser().parse(txBytes);

            // 1. Parse and validate the signed transaction
            final var signedTransaction = session.signedParser().parse(tx.signedTransactionBytes());
            checker.checkSignedTransaction(signedTransaction);

            // 2. Parse and validate the TransactionBody.
            //    TODO we need to use a custom tx body parser, but don't yet.
            //    We use a custom TransactionBodyParser that skips the "data" field. We don't
            //    need it at this stage of processing, and so parsing it is a waste of time.
            //    Also check that the transaction ID is valid, and other fields. Ask the
            //    TokenService for account information to validate that the account has enough
            //    HBAR to pay the node & network fees. And verify the start transaction time is
            //    valid.
            final var txBody = session.txBodyParser().parse(signedTransaction.bodyBytes());
            final var account = accountService.lookupAccount(txBody.transactionID().accountID());
            checker.checkTransactionBody(txBody, account);

            // 3. Validate signature
            checker.checkSignatures(tx.signedTransactionBytes(), signedTransaction.sigMap(), account.keys());

            // 4. If signatures all check out, then check the throttles. Ya, I'd like to check
            //    throttles way back on step 1, but we need to verify whether the account is
            //    a privileged account (less than 0.0.100) and if so skip throttles. Without
            //    a way to authenticate the payload any earlier than step 3, we have to do it now.
            checker.checkThrottles(txBody.data().kind());

            // 5. Submit to platform
            final var success = platform.createTransaction(txBytes);
            if (!success) {
                throw new BackPressureException();
            }

            // 6. And if we made it all the way here without any kind of trauma, then we can create
            //    a nice response and send it back to the caller. Yippee.

            // TODO We should *DEFINITELY* have metrics for how often every response code enum is used.
            // TODO Need to create a real protobuf message here
            // TODO We need to include fee calculations here, not sure what to return...?
            final var response = new TransactionResponse(ResponseCodeEnum.Success, 100);
            return new byte[0];
        } catch (MalformedProtobufException ex) {
            // TODO How to cost this?
            final var response = new TransactionResponse(ResponseCodeEnum.BadEncoding, 100);
            return new byte[0];
        } catch (PreCheckException ex) {
            // TODO How to cost this?
            final var response = new TransactionResponse(ex.responseCode(), 100);
            return new byte[0];
        } catch (Throwable th) {
            // We should NEVER hit this. If we do, log it vigorously. Something very wrong happened.
            // TODO How to cost this?
            final var response = new TransactionResponse(ResponseCodeEnum.Unknown, 100);
            return new byte[0];
        }
    }
}
