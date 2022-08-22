package com.hedera.hashgraph.base;

import java.util.Objects;

public abstract class TransactionMetadata {
    private final boolean failed;

    protected TransactionMetadata(boolean failed) {
        this.failed = failed;
    }

    public boolean failed() {
        return failed;
    }

    public static final class UnknownErrorTransactionMetadata extends TransactionMetadata {
        private final Throwable throwable;

        public UnknownErrorTransactionMetadata(Throwable th) {
            super(true);
            this.throwable = Objects.requireNonNull(th);
        }

        public Throwable cause() {
            return throwable;
        }
    }
}
