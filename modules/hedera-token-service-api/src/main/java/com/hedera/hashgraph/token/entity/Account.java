package com.hedera.hashgraph.token.entity;

import com.hedera.hashgraph.hapi.model.Key;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

/**
 * An Account entity represents a Hedera Account.
 */
public interface Account {
    long shardNumber();
    long realmNumber();
    long accountNumber();
    Optional<byte[]> alias();

    /**
     * Gets whether this is a "hollow" account. A hollow account is an account that was created
     * automatically as a result of a crypto transfer (or token transfer, or other transfer of value)
     * into a non-existent account via alias or virtual address.
     *
     * @return True if this is a hollow account
     */
    boolean isHollow();

    /**
     * The keys on the account. This may return an empty {@link Optional} if the account is
     * a "hollow" account (as determined by {@link #isHollow()()}).
     *
     * @return An optional key list. This will always be set unless the account is hollow.
     */
    @NonNull
    Optional<Key> key();

    /**
     * Gets the expiry of the account, in millis from the epoch.
     *
     * @return The expiry of the account in millis from the epoch.
     */
    long expiry();

    /**
     * Gets the hbar balance of the account. The balance will always be non-negative.
     *
     * @return The hbar balance (in tinybar)
     */
    long balance(); // Maybe have something more useful here for hbar that can convert tinybar etc.

    // long autoRenewSecs(); // what is the difference between this and expiry??

    @NonNull String memo();
    boolean isDeleted();
    boolean isSmartContract();
    boolean isReceiverSigRequired();

    long proxyAccountNumber();
    long numberOfOwnedNfts();

    int maxAutoAssociations();
    int usedAutoAssociations();
    int numAssociations();
    int numPositiveBalances(); // WHAT IS THIS?

    long ethereumNonce();

    long stakedToMe();
    long stakePeriodStart();
    long stakedNum();
    boolean declineReward();
    long stakeAtStartOfLastRewardedPeriod();

    long autoRenewAccountNumber();

    /*
    int numContractKvPairs
     */

    /*
    private int[] firstUint256Key;
    // Number of the low-order bytes in firstUint256Key that contain ones
    private byte firstUint256KeyNonZeroBytes;
    */

    /*
    private long headNftId;
    private long headNftSerialNum;
    */

    /*
    // C.f. https://github.com/hashgraph/hedera-services/issues/2842; we may want to migrate
    // these per-account maps to top-level maps using the "linked-list" values idiom
    private Map<Long, Long> cryptoAllowances = Collections.emptyMap();
    private Map<FcTokenAllowanceId, Long> fungibleTokenAllowances = Collections.emptyMap();
    private Set<FcTokenAllowanceId> approveForAllNfts = Collections.emptySet();
    */

    /**
     * Creates an AccountBuilder that clones all state in this instance, allowing the user to override
     * only the specific state that they choose to override.
     *
     * @return A non-null builder pre-initialized with all state in this instance.
     */
    @NonNull AccountBuilder copy();
}
