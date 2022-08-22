package com.hedera.hashgraph.base;

import com.hedera.hashgraph.hapi.model.base.Transaction;

import java.util.Objects;

public abstract class TransactionMetadata {
    private final boolean failed;
    private final Transaction tx;

    protected TransactionMetadata(Transaction tx, boolean failed) {
        this.tx = tx;
        this.failed = failed;
    }

    public Transaction transaction() {
        return tx;
    }

    public boolean failed() {
        return failed;
    }

    public static final class UnknownErrorTransactionMetadata extends TransactionMetadata {
        private final Throwable throwable;

        public UnknownErrorTransactionMetadata(Throwable th) {
            super(null, true);
            this.throwable = Objects.requireNonNull(th);
        }

        public Throwable cause() {
            return throwable;
        }
    }
}
