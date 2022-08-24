package com.hedera.hashgraph.app.workflows.query;

import com.hedera.hashgraph.app.SessionContext;
import com.hedera.hashgraph.app.workflows.ingest.BackPressureException;
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
 * A workflow for processing queries.
 */
public final class QueryWorkflow {
    public byte[] handleQuery(SessionContext session, byte[] queryBytes) {
        return new byte[0];
    }
}
