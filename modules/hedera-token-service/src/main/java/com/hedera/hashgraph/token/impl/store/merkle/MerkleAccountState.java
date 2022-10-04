package com.hedera.hashgraph.token.impl.store.merkle;

import com.swirlds.common.exceptions.MutabilityException;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.common.merkle.MerkleLeaf;
import com.swirlds.common.merkle.impl.PartialMerkleLeaf;

import java.io.IOException;
import java.util.*;

public class MerkleAccountState extends PartialMerkleLeaf implements MerkleLeaf {
    private static final int MAX_CONCEIVABLE_MEMO_UTF8_BYTES = 1_024;

    static final int RELEASE_0230_VERSION = 10;
    static final int RELEASE_0250_ALPHA_VERSION = 11;
    static final int RELEASE_0250_VERSION = 12;
    static final int RELEASE_0260_VERSION = 13;
    static final int RELEASE_0270_VERSION = 14;
    private static final int CURRENT_VERSION = RELEASE_0270_VERSION;
    static final long RUNTIME_CONSTRUCTABLE_ID = 0x354cfc55834e7f12L;

    public static final String DEFAULT_MEMO = "";
    private static final byte[] DEFAULT_ALIAS = new byte[0];

    private byte[] key;
    private long expiry;
    private long hbarBalance;
    private long autoRenewSecs;
    private String memo = DEFAULT_MEMO;
    private boolean deleted;
    private boolean smartContract;
    private boolean receiverSigRequired;
    private long proxy;
    private long nftsOwned;
    private long number;
    private byte[] alias = DEFAULT_ALIAS;
    private int numContractKvPairs;
    // The first key in the doubly-linked list of this contract's storage mappings; null if this
    // account is not a contract, or a contract with no storage
    private int[] firstUint256Key;
    // Number of the low-order bytes in firstUint256Key that contain ones
    private byte firstUint256KeyNonZeroBytes;

    private int maxAutoAssociations;
    private int usedAutoAssociations;
    private int numAssociations;
    private int numPositiveBalances;
    private long headTokenId;
    private int numTreasuryTitles;
    private long headNftId;
    private long headNftSerialNum;
    private long ethereumNonce;
    private long stakedToMe;
    // default value and if this account stakes to an account value is -1. It will be set to the
    // time when the account
    // starts staking to a node.
    private long stakePeriodStart = -1;
    // if -ve we are staking to a node, if +ve we are staking to an account and 0 if not staking to
    // anyone.
    // When staking to a node it is stored as -node-1 in order to differentiate nodeId=0
    private long stakedNum;
    private boolean declineReward;
    private long stakeAtStartOfLastRewardedPeriod = -1;

    // C.f. https://github.com/hashgraph/hedera-services/issues/2842; we may want to migrate
    // these per-account maps to top-level maps using the "linked-list" values idiom
    private Map<Long, Long> cryptoAllowances = Collections.emptyMap();
    private Map<FcTokenAllowanceId, Long> fungibleTokenAllowances = Collections.emptyMap();
    private Set<FcTokenAllowanceId> approveForAllNfts = Collections.emptySet();

    private long autoRenewAccount;

    public MerkleAccountState() {
        // RuntimeConstructable
    }

    public MerkleAccountState(final MerkleAccountState that) {
        this.key = that.key;
        this.expiry = that.expiry;
        this.hbarBalance = that.hbarBalance;
        this.autoRenewSecs = that.autoRenewSecs;
        this.memo = that.memo;
        this.deleted = that.deleted;
        this.smartContract = that.smartContract;
        this.receiverSigRequired = that.receiverSigRequired;
        this.proxy = that.proxy;
        this.number = that.number;
        this.maxAutoAssociations = that.maxAutoAssociations;
        this.usedAutoAssociations = that.usedAutoAssociations;
        this.alias = that.alias;
        this.numContractKvPairs = that.numContractKvPairs;
        this.cryptoAllowances = that.cryptoAllowances;
        this.fungibleTokenAllowances = that.fungibleTokenAllowances;
        this.approveForAllNfts = that.approveForAllNfts;
        this.firstUint256Key = that.firstUint256Key;
        this.firstUint256KeyNonZeroBytes = that.firstUint256KeyNonZeroBytes;
        this.nftsOwned = that.nftsOwned;
        this.numAssociations = that.numAssociations;
        this.numPositiveBalances = that.numPositiveBalances;
        this.headTokenId = that.headTokenId;
        this.numTreasuryTitles = that.numTreasuryTitles;
        this.ethereumNonce = that.ethereumNonce;
        this.autoRenewAccount = that.autoRenewAccount;
        this.headNftId = that.headNftId;
        this.headNftSerialNum = that.headNftSerialNum;
        this.stakedToMe = that.stakedToMe;
        this.stakePeriodStart = that.stakePeriodStart;
        this.stakedNum = that.stakedNum;
        this.declineReward = that.declineReward;
        this.stakeAtStartOfLastRewardedPeriod = that.stakeAtStartOfLastRewardedPeriod;
    }

    public MerkleAccountState(
            final byte[] key,
            final long expiry,
            final long hbarBalance,
            final long autoRenewSecs,
            final String memo,
            final boolean deleted,
            final boolean smartContract,
            final boolean receiverSigRequired,
            final long proxy,
            final int number,
            final int maxAutoAssociations,
            final int usedAutoAssociations,
            final byte[] alias,
            final int numContractKvPairs,
            final Map<Long, Long> cryptoAllowances,
            final Map<FcTokenAllowanceId, Long> fungibleTokenAllowances,
            final Set<FcTokenAllowanceId> approveForAllNfts,
            final int[] firstUint256Key,
            final byte firstUint256KeyNonZeroBytes,
            final long nftsOwned,
            final int numAssociations,
            final int numPositiveBalances,
            final long headTokenId,
            final int numTreasuryTitles,
            final long ethereumNonce,
            final long autoRenewAccount,
            final long headNftId,
            final long headNftSerialNum,
            final long stakedToMe,
            final long stakePeriodStart,
            final long stakedNum,
            final boolean declineReward,
            final long stakeAtStartOfLastRewardedPeriod) {
        this.key = key;
        this.expiry = expiry;
        this.hbarBalance = hbarBalance;
        this.autoRenewSecs = autoRenewSecs;
        this.memo = Optional.ofNullable(memo).orElse(DEFAULT_MEMO);
        this.deleted = deleted;
        this.smartContract = smartContract;
        this.receiverSigRequired = receiverSigRequired;
        this.proxy = proxy;
        this.number = number;
        this.maxAutoAssociations = maxAutoAssociations;
        this.usedAutoAssociations = usedAutoAssociations;
        this.alias = Optional.ofNullable(alias).orElse(DEFAULT_ALIAS);
        this.numContractKvPairs = numContractKvPairs;
        this.cryptoAllowances = cryptoAllowances;
        this.fungibleTokenAllowances = fungibleTokenAllowances;
        this.approveForAllNfts = approveForAllNfts;
        this.firstUint256Key = firstUint256Key;
        this.firstUint256KeyNonZeroBytes = firstUint256KeyNonZeroBytes;
        this.nftsOwned = nftsOwned;
        this.numAssociations = numAssociations;
        this.numPositiveBalances = numPositiveBalances;
        this.headTokenId = headTokenId;
        this.numTreasuryTitles = numTreasuryTitles;
        this.ethereumNonce = ethereumNonce;
        this.autoRenewAccount = autoRenewAccount;
        this.headNftId = headNftId;
        this.headNftSerialNum = headNftSerialNum;
        this.stakedToMe = stakedToMe;
        this.stakePeriodStart = stakePeriodStart;
        this.stakedNum = stakedNum;
        this.declineReward = declineReward;
        this.stakeAtStartOfLastRewardedPeriod = stakeAtStartOfLastRewardedPeriod;
    }

    /* --- MerkleLeaf --- */
    @Override
    public long getClassId() {
        return RUNTIME_CONSTRUCTABLE_ID;
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public int getMinimumSupportedVersion() {
        return RELEASE_0230_VERSION;
    }

    @Override
    public void deserialize(final SerializableDataInputStream in, final int version)
            throws IOException {
        // TODO
    }

    @Override
    public void serialize(final SerializableDataOutputStream out) throws IOException {
        // TODO
    }

    /* --- Copyable --- */
    public MerkleAccountState copy() {
        setImmutable(true);
        return new MerkleAccountState(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || MerkleAccountState.class != o.getClass()) {
            return false;
        }

        var that = (MerkleAccountState) o;

        return this.number == that.number
                && this.expiry == that.expiry
                && this.hbarBalance == that.hbarBalance
                && this.autoRenewSecs == that.autoRenewSecs
                && Objects.equals(this.memo, that.memo)
                && this.deleted == that.deleted
                && this.smartContract == that.smartContract
                && this.receiverSigRequired == that.receiverSigRequired
                && Objects.equals(this.proxy, that.proxy)
                && this.nftsOwned == that.nftsOwned
                && this.numContractKvPairs == that.numContractKvPairs
                && this.ethereumNonce == that.ethereumNonce
                && this.maxAutoAssociations == that.maxAutoAssociations
                && this.usedAutoAssociations == that.usedAutoAssociations
//                && equalUpToDecodability(this.key, that.key)
//                && Objects.equals(this.alias, that.alias)
                && Objects.equals(this.cryptoAllowances, that.cryptoAllowances)
                && Objects.equals(this.fungibleTokenAllowances, that.fungibleTokenAllowances)
                && Objects.equals(this.approveForAllNfts, that.approveForAllNfts)
                && Arrays.equals(this.firstUint256Key, that.firstUint256Key)
                && this.numAssociations == that.numAssociations
                && this.numPositiveBalances == that.numPositiveBalances
                && this.headTokenId == that.headTokenId
                && this.numTreasuryTitles == that.numTreasuryTitles
                && Objects.equals(this.autoRenewAccount, that.autoRenewAccount)
                && this.headNftId == that.headNftId
                && this.headNftSerialNum == that.headNftSerialNum
                && this.stakedToMe == that.stakedToMe
                && this.stakePeriodStart == that.stakePeriodStart
                && this.stakedNum == that.stakedNum
                && this.declineReward == that.declineReward
                && this.stakeAtStartOfLastRewardedPeriod == that.stakeAtStartOfLastRewardedPeriod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
//                key,
                expiry,
                hbarBalance,
                autoRenewSecs,
                memo,
                deleted,
                smartContract,
                receiverSigRequired,
                proxy,
                nftsOwned,
                number,
                maxAutoAssociations,
                usedAutoAssociations,
//                alias,
                cryptoAllowances,
                fungibleTokenAllowances,
                approveForAllNfts,
                Arrays.hashCode(firstUint256Key),
                numAssociations,
                numPositiveBalances,
                headTokenId,
                numTreasuryTitles,
                ethereumNonce,
                autoRenewAccount,
                headNftId,
                headNftSerialNum,
                stakedToMe,
                stakePeriodStart,
                stakedNum,
                declineReward,
                stakeAtStartOfLastRewardedPeriod);
    }

    /* --- Bean --- */

    public long number() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public void setAlias(byte[] alias) {
        this.alias = alias;
    }

    public byte[] key() {
        return key;
    }

    public long expiry() {
        return expiry;
    }

    public long balance() {
        return hbarBalance;
    }

    public long autoRenewSecs() {
        return autoRenewSecs;
    }

    public String memo() {
        return memo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isSmartContract() {
        return smartContract;
    }

    public boolean isReceiverSigRequired() {
        return receiverSigRequired;
    }

    public long proxy() {
        return proxy;
    }

    public long ethereumNonce() {
        return ethereumNonce;
    }

    public long nftsOwned() {
        return nftsOwned;
    }

    public byte[] getAlias() {
        return alias;
    }

    public void setAccountKey(byte[] key) {
        assertMutable("key");
        this.key = key;
    }

    public void setExpiry(long expiry) {
        assertMutable("expiry");
        this.expiry = expiry;
    }

    public void setHbarBalance(long hbarBalance) {
        assertMutable("hbarBalance");
        this.hbarBalance = hbarBalance;
    }

    public void setAutoRenewSecs(long autoRenewSecs) {
        assertMutable("autoRenewSecs");
        this.autoRenewSecs = autoRenewSecs;
    }

    public void setMemo(String memo) {
        assertMutable("memo");
        this.memo = memo;
    }

    public void setEthereumNonce(long ethereumNonce) {
        assertMutable("ethereumNonce");
        this.ethereumNonce = ethereumNonce;
    }

    public void setDeleted(boolean deleted) {
        assertMutable("isSmartContract");
        this.deleted = deleted;
    }

    public void setSmartContract(boolean smartContract) {
        assertMutable("isSmartContract");
        this.smartContract = smartContract;
    }

    public void setReceiverSigRequired(boolean receiverSigRequired) {
        assertMutable("isReceiverSigRequired");
        this.receiverSigRequired = receiverSigRequired;
    }

    public void setProxy(long proxy) {
        assertMutable("proxy");
        this.proxy = proxy;
    }

    public void setNftsOwned(final long nftsOwned) {
        assertMutable("nftsOwned");
        this.nftsOwned = nftsOwned;
    }

    public int getNumAssociations() {
        return numAssociations;
    }

    public void setNumAssociations(final int numAssociations) {
        assertMutable("numAssociations");
        this.numAssociations = numAssociations;
    }

    public int getNumPositiveBalances() {
        return numPositiveBalances;
    }

    public void setNumPositiveBalances(final int numPositiveBalances) {
        assertMutable("numPositiveBalances");
        this.numPositiveBalances = numPositiveBalances;
    }

    public long getHeadTokenId() {
        return headTokenId;
    }

    public void setHeadTokenId(final long headTokenId) {
        assertMutable("headTokenId");
        this.headTokenId = headTokenId;
    }

    public long getHeadNftId() {
        return headNftId;
    }

    public void setHeadNftId(final long headNftId) {
        assertMutable("headNftId");
        this.headNftId = headNftId;
    }

    public long getHeadNftSerialNum() {
        return headNftSerialNum;
    }

    public void setHeadNftSerialNum(final long headNftSerialNum) {
        assertMutable("headNftSerialNum");
        this.headNftSerialNum = headNftSerialNum;
    }

    public int getNumContractKvPairs() {
        return numContractKvPairs;
    }

    public void setNumContractKvPairs(int numContractKvPairs) {
        assertMutable("numContractKvPairs");
        this.numContractKvPairs = numContractKvPairs;
    }

    public int getMaxAutomaticAssociations() {
        return maxAutoAssociations;
    }

    public int getUsedAutomaticAssociations() {
        return usedAutoAssociations;
    }

    public void setMaxAutomaticAssociations(int maxAutomaticAssociations) {
        assertMutable("maxAutomaticAssociations");
        this.maxAutoAssociations = maxAutomaticAssociations;
    }

    public void setUsedAutomaticAssociations(int usedAutoAssociations) {
        assertMutable("usedAutomaticAssociations");
        this.usedAutoAssociations = usedAutoAssociations;
    }

    public Map<Long, Long> getCryptoAllowances() {
        return Collections.unmodifiableMap(cryptoAllowances);
    }

    public void setCryptoAllowances(final SortedMap<Long, Long> cryptoAllowances) {
        assertMutable("cryptoAllowances");
        this.cryptoAllowances = cryptoAllowances;
    }

    public Map<Long, Long> getCryptoAllowancesUnsafe() {
        return cryptoAllowances;
    }

    public void setCryptoAllowancesUnsafe(final Map<Long, Long> cryptoAllowances) {
        assertMutable("cryptoAllowances");
        this.cryptoAllowances = cryptoAllowances;
    }

    public boolean isTokenTreasury() {
        return numTreasuryTitles > 0;
    }

    public int getNumTreasuryTitles() {
        return numTreasuryTitles;
    }

    public void setNumTreasuryTitles(final int numTreasuryTitles) {
        assertMutable("numTreasuryTitles");
        this.numTreasuryTitles = numTreasuryTitles;
    }

    public Set<FcTokenAllowanceId> getApproveForAllNfts() {
        return Collections.unmodifiableSet(approveForAllNfts);
    }

    public void setApproveForAllNfts(final Set<FcTokenAllowanceId> approveForAllNfts) {
        assertMutable("ApproveForAllNfts");
        this.approveForAllNfts = approveForAllNfts;
    }

    public Set<FcTokenAllowanceId> getApproveForAllNftsUnsafe() {
        return approveForAllNfts;
    }

    public Map<FcTokenAllowanceId, Long> getFungibleTokenAllowances() {
        return Collections.unmodifiableMap(fungibleTokenAllowances);
    }

    public void setFungibleTokenAllowances(
            final SortedMap<FcTokenAllowanceId, Long> fungibleTokenAllowances) {
        assertMutable("fungibleTokenAllowances");
        this.fungibleTokenAllowances = fungibleTokenAllowances;
    }

    public Map<FcTokenAllowanceId, Long> getFungibleTokenAllowancesUnsafe() {
        return fungibleTokenAllowances;
    }

    public void setFungibleTokenAllowancesUnsafe(
            final Map<FcTokenAllowanceId, Long> fungibleTokenAllowances) {
        assertMutable("fungibleTokenAllowances");
        this.fungibleTokenAllowances = fungibleTokenAllowances;
    }

//    public ContractKey getFirstContractStorageKey() {
//        return firstUint256Key == null
//                ? null
//                : new ContractKey(BitPackUtils.numFromCode(number), firstUint256Key);
//    }

    public int[] getFirstUint256Key() {
        return firstUint256Key;
    }

    public void setFirstUint256Key(final int[] firstUint256Key) {
        assertMutable("firstUint256Key");
        this.firstUint256Key = firstUint256Key;
//        if (firstUint256Key != null) {
//            firstUint256KeyNonZeroBytes = computeNonZeroBytes(firstUint256Key);
//        } else {
//            firstUint256KeyNonZeroBytes = 0;
//        }
    }

    public boolean hasAutoRenewAccount() {
        return false;
//        return autoRenewAccount != null && !autoRenewAccount.equals(MISSING_ENTITY_ID);
    }

    public long getAutoRenewAccount() {
        return autoRenewAccount;
    }

    public void setAutoRenewAccount(final long autoRenewAccount) {
        this.autoRenewAccount = autoRenewAccount;
    }

    public long getStakedToMe() {
        return stakedToMe;
    }

    public void setStakedToMe(final long stakedToMe) {
        assertMutable("stakedToMe");
        this.stakedToMe = stakedToMe;
    }

    public long getStakePeriodStart() {
        return stakePeriodStart;
    }

    public void setStakePeriodStart(final long stakePeriodStart) {
        assertMutable("stakePeriodStart");
        this.stakePeriodStart = stakePeriodStart;
    }

    public long getStakedNum() {
        return stakedNum;
    }

    public void setStakedNum(final long stakedNum) {
        assertMutable("stakedNum");
        this.stakedNum = stakedNum;
    }

    public boolean isDeclineReward() {
        return declineReward;
    }

    public void setDeclineReward(final boolean declineReward) {
        assertMutable("declineReward");
        this.declineReward = declineReward;
    }

    public long getStakeAtStartOfLastRewardedPeriod() {
        return stakeAtStartOfLastRewardedPeriod;
    }

    public void setStakeAtStartOfLastRewardedPeriod(final long stakeAtStartOfLastRewardedPeriod) {
        assertMutable("balanceAtStartOfLastRewardedPeriod");
        this.stakeAtStartOfLastRewardedPeriod = stakeAtStartOfLastRewardedPeriod;
    }

    private void assertMutable(String proximalField) {
        if (isImmutable()) {
            throw new MutabilityException(
                    "Cannot set " + proximalField + " on an immutable account state!");
        }
    }
}
