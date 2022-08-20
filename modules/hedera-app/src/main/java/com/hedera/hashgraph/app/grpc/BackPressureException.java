package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.app.workflows.ingest.PreCheckException;
import com.hedera.hashgraph.hapi.model.base.ResponseCodeEnum;

/**
 * Thrown if backpressure is being applied by the platform.
 */
public class BackPressureException extends PreCheckException {
    public BackPressureException() {
        super(ResponseCodeEnum.Busy, "Server busy. Backpressure is being applied. Try again later.");
    }
}
