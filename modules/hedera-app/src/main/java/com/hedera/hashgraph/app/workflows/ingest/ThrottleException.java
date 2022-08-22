package com.hedera.hashgraph.app.workflows.ingest;

import com.hedera.hashgraph.app.workflows.ingest.PreCheckException;
import com.hedera.hashgraph.hapi.model.base.ResponseCodeEnum;

/**
 * Thrown if a throttle is exceeded.
 */
public class ThrottleException extends PreCheckException {
    public ThrottleException(String message) {
        // TODO Not sure.
        super(ResponseCodeEnum.BUSY, message);
    }
}
