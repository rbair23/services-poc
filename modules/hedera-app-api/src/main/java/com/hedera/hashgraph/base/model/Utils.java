package com.hedera.hashgraph.base.model;

import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.hapi.model.Key;
import com.hedera.hashgraph.hapi.model.KeyList;
import com.hedera.hashgraph.hapi.model.base.Timestamp;
import com.hedera.hashgraph.hapi.schema.AccountIDSchema;
import com.hedera.hashgraph.hapi.schema.KeySchema;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class Utils {

    private Utils() {

    }

    @NonNull
    public static AccountID accountID(long accountNum) {
        if (accountNum < 1) {
            throw new IllegalArgumentException("Account must be positive");
        }

        return new AccountID(0, 0,
                new OneOf<>(
                        AccountID.AccountOneOfType.ACCOUNT_NUM,
                        accountNum));
    }

    @NonNull
    public static AccountID accountID(@NonNull byte[] alias) {
        return new AccountID(0, 0,
                new OneOf<>(
                        AccountID.AccountOneOfType.ALIAS,
                        alias));
    }

    @NonNull
    public static AccountID accountID(@NonNull String alias) {
        return new AccountID(0, 0,
                new OneOf<>(
                        AccountID.AccountOneOfType.ALIAS,
                        alias.getBytes(StandardCharsets.UTF_8)));
    }

    @NonNull
    public static Key ecdsa(@NonNull byte[] keyBytes) {
        return new Key.Builder().ecdsaSecp256k1(ByteBuffer.wrap(keyBytes)).build();
    }

    @NonNull
    public static Key ecdsa384(@NonNull byte[] keyBytes) {
        return new Key(new OneOf<>(Key.KeyOneOfType.ECDSA_384, keyBytes));
    }

    @NonNull
    public static Key ed25519(@NonNull byte[] keyBytes) {
        return new Key(new OneOf<>(Key.KeyOneOfType.ED25519, keyBytes));
    }

    @NonNull
    public static Key keys(@NonNull List<Key> keys) {
        return new Key(new OneOf<>(Key.KeyOneOfType.KEY_LIST, new KeyList(keys)));
    }
}
