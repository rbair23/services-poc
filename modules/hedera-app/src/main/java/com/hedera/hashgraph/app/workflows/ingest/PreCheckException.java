package com.hedera.hashgraph.app.workflows.ingest;

import com.hedera.hashgraph.hapi.model.base.ResponseCodeEnum;

/**
 * Thrown if the request itself is bad. The protobuf decoded correctly, but it failed one or more of
 * the ingestion pipeline pre-checks.
 */
public class PreCheckException extends Exception {
    private ResponseCodeEnum responseCode;

    public PreCheckException(ResponseCodeEnum responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public ResponseCodeEnum responseCode() {
        return responseCode;
    }
}
