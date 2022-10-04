package com.hedera.hashgraph.token.impl.store.merkle;

import com.swirlds.common.merkle.MerkleInternal;
import com.swirlds.common.merkle.MerkleNode;
import com.swirlds.common.merkle.impl.PartialNaryMerkleInternal;
import com.swirlds.common.merkle.utility.Keyed;
import java.util.*;

// NOTE: This is NOT exported from the module
public class MerkleAccount extends PartialNaryMerkleInternal
		implements MerkleInternal, Keyed<Long> {
//	private static final Logger log = LogManager.getLogger(MerkleAccount.class);

	static Runnable stackDump = Thread::dumpStack;

	private static final int RELEASE_0240_VERSION = 4;
	static final int MERKLE_VERSION = RELEASE_0240_VERSION;

	static final long RUNTIME_CONSTRUCTABLE_ID = 0x950bcf7255691908L;

	@Override
	public Long getKey() {
		return state().number();
	}

	@Override
	public void setKey(Long num) {
		state().setNumber(num);
	}

	// Order of Merkle node children
	public static final class ChildIndices {
		private static final int STATE = 0;
		static final int NUM_POST_0240_CHILDREN = 2;

		private ChildIndices() {
			throw new UnsupportedOperationException("Utility Class");
		}
	}

	public MerkleAccount(final List<MerkleNode> children, final MerkleAccount immutableAccount) {
		super(immutableAccount);
		addDeserializedChildren(children, MERKLE_VERSION);
	}

	public MerkleAccount(final List<MerkleNode> children) {
		addDeserializedChildren(children, MERKLE_VERSION);
	}

	public MerkleAccount() {
		addDeserializedChildren(
				List.of(new MerkleAccountState()/*, new FCQueue<ExpirableTxnRecord>()*/),
				MERKLE_VERSION);
	}

	/* --- MerkleInternal --- */
	@Override
	public long getClassId() {
		return RUNTIME_CONSTRUCTABLE_ID;
	}

	@Override
	public int getVersion() {
		return MERKLE_VERSION;
	}

	@Override
	public int getMinimumChildCount() {
		return ChildIndices.NUM_POST_0240_CHILDREN;
	}

	// --- FastCopyable ---
	@Override
	public MerkleAccount copy() {
		if (isImmutable()) {
//			final var msg =
//					String.format(
//							"Copy called on immutable MerkleAccount by thread '%s'! Payer records"
//									+ " mutable? %s",
//							Thread.currentThread().getName(),
//							records().isImmutable() ? "NO" : "YES");
//			log.warn(msg);
			/* Ensure we get this stack trace in case a caller incorrectly suppresses the exception. */
			stackDump.run();
			throw new IllegalStateException("Tried to make a copy of an immutable MerkleAccount!");
		}

		setImmutable(true);
		return new MerkleAccount(List.of(state().copy()/*, records().copy()*/), this);
	}

	// ---- Object ----
	@Override
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (o == null || MerkleAccount.class != o.getClass()) {
			return false;
		}
		final var that = (MerkleAccount) o;
		return this.state().equals(that.state());// && this.records().equals(that.records());
	}

	@Override
	public int hashCode() {
		return Objects.hash(state()); //, records());
	}

	/* ----  Merkle children  ---- */
	public void forgetThirdChildIfPlaceholder() {
//		if (getNumberOfChildren() == 3) {
//			if (getChild(2) instanceof MerkleAccountTokensPlaceholder) {
//				addDeserializedChildren(List.of(state(), records()), RELEASE_0240_VERSION);
//			} else {
//				log.error(
//						"Third child of account {} had unexpected type {}",
//						state(),
//						getChild(2).getClass().getSimpleName());
//			}
//		}
	}

	public MerkleAccountState state() {
		return getChild(ChildIndices.STATE);
	}

//	public FCQueue<ExpirableTxnRecord> records() {
//		return getChild(ChildIndices.RECORDS);
//	}

	// ----  Bean  ----
	public long getNftsOwned() {
		return state().nftsOwned();
	}

	public void setNftsOwned(final long nftsOwned) {
		throwIfImmutable("Cannot change this account's owned NFTs if it's immutable.");
		state().setNftsOwned(nftsOwned);
	}

	public boolean isTokenTreasury() {
		return state().isTokenTreasury();
	}

	public int getNumTreasuryTitles() {
		return state().getNumTreasuryTitles();
	}

	public void setNumTreasuryTitles(final int treasuryTitles) {
		// This will throw MutabilityException if we are immutable
		state().setNumTreasuryTitles(treasuryTitles);
	}

	public String getMemo() {
		return state().memo();
	}

	public void setMemo(final String memo) {
		throwIfImmutable("Cannot change this account's memo if it's immutable.");
		state().setMemo(memo);
	}

	public boolean isSmartContract() {
		return state().isSmartContract();
	}

	public void setSmartContract(final boolean smartContract) {
		throwIfImmutable("Cannot change this account's smart contract if it's immutable.");
		state().setSmartContract(smartContract);
	}

	public byte[] getAlias() {
		return state().getAlias();
	}

	public void setAlias(final byte[] alias) {
		throwIfImmutable("Cannot change this account's alias if it's immutable.");
		Objects.requireNonNull(alias);
		state().setAlias(alias);
	}

	public long getEthereumNonce() {
		return state().ethereumNonce();
	}

	public void setEthereumNonce(final long ethereumNonce) {
		throwIfImmutable("Cannot change this account's ethereumNonce if it's immutable.");
		state().setEthereumNonce(ethereumNonce);
	}

	public int getNumAssociations() {
		return state().getNumAssociations();
	}

	public void setNumAssociations(final int numAssociations) {
		throwIfImmutable("Cannot change this account's numAssociations if it's immutable");
		state().setNumAssociations(numAssociations);
	}

	public int getNumPositiveBalances() {
		return state().getNumPositiveBalances();
	}

	public void setNumPositiveBalances(final int numPositiveBalances) {
		throwIfImmutable("Cannot change this account's numPositiveBalances if it's immutable");
		state().setNumPositiveBalances(numPositiveBalances);
	}

	public long getHeadTokenId() {
		return state().getHeadTokenId();
	}

	public void setHeadTokenId(final long headTokenId) {
		throwIfImmutable("Cannot change this account's headTokenId if it's immutable");
		state().setHeadTokenId(headTokenId);
	}

	public long getHeadNftTokenNum() {
		return state().getHeadNftId();
	}

//	public void setHeadNftId(final long headNftId) {
//		throwIfImmutable("Cannot change this account's headNftId if it's immutable");
//		state().setHeadNftId(headNftId);
//	}
//
//	public EntityNumPair getHeadNftKey() {
//		return EntityNumPair.fromLongs(getHeadNftTokenNum(), getHeadNftSerialNum());
//	}

	public long getHeadNftSerialNum() {
		return state().getHeadNftSerialNum();
	}

//	public void setHeadNftSerialNum(final long headNftSerialNum) {
//		throwIfImmutable("Cannot change this account's headNftSerialNum if it's immutable");
//		state().setHeadNftSerialNum(headNftSerialNum);
//	}
//
//	public EntityNumPair getLatestAssociation() {
//		return EntityNumPair.fromLongs(state().number(), getHeadTokenId());
//	}

	public long getBalance() {
		return state().balance();
	}

	public void setBalance(final long balance) { //throws NegativeAccountBalanceException {
		if (balance < 0) {
//			throw new NegativeAccountBalanceException(
//					String.format("Illegal balance: %d!", balance));
		}
		throwIfImmutable("Cannot change this account's hbar balance if it's immutable.");
		state().setHbarBalance(balance);
	}

	public void setBalanceUnchecked(final long balance) {
		if (balance < 0) {
			throw new IllegalArgumentException("Cannot set an ℏ balance to " + balance);
		}
		throwIfImmutable("Cannot change this account's hbar balance if it's immutable.");
		state().setHbarBalance(balance);
	}

	public boolean isReceiverSigRequired() {
		return state().isReceiverSigRequired();
	}

	public void setReceiverSigRequired(final boolean receiverSigRequired) {
		throwIfImmutable(
				"Cannot change this account's receiver signature required setting if it's"
						+ " immutable.");
		state().setReceiverSigRequired(receiverSigRequired);
	}

	public byte[] getAccountKey() {
		return state().key();
	}

	public void setAccountKey(final byte[] key) {
		throwIfImmutable("Cannot change this account's key if it's immutable.");
		state().setAccountKey(key);
	}

	public long number() {
		return state().number();
	}

	public long getProxy() {
		return state().proxy();
	}

	public void setProxy(final long proxy) {
		throwIfImmutable("Cannot change this account's proxy if it's immutable.");
		state().setProxy(proxy);
	}

	public long getAutoRenewSecs() {
		return state().autoRenewSecs();
	}

	public void setAutoRenewSecs(final long autoRenewSecs) {
		throwIfImmutable("Cannot change this account's auto renewal seconds if it's immutable.");
		state().setAutoRenewSecs(autoRenewSecs);
	}

	public boolean isDeleted() {
		return state().isDeleted();
	}

	public void setDeleted(final boolean deleted) {
		throwIfImmutable("Cannot change this account's deleted status if it's immutable.");
		state().setDeleted(deleted);
	}

	public long getExpiry() {
		return state().expiry();
	}

	public void setExpiry(final long expiry) {
		throwIfImmutable("Cannot change this account's expiry time if it's immutable.");
		state().setExpiry(expiry);
	}

	public int getMaxAutomaticAssociations() {
		return state().getMaxAutomaticAssociations();
	}

	public void setMaxAutomaticAssociations(int maxAutomaticAssociations) {
		state().setMaxAutomaticAssociations(maxAutomaticAssociations);
	}

	public int getUsedAutoAssociations() {
		return state().getUsedAutomaticAssociations();
	}

	public void setUsedAutomaticAssociations(final int usedAutoAssociations) {
		if (usedAutoAssociations < 0 || usedAutoAssociations > getMaxAutomaticAssociations()) {
			throw new IllegalArgumentException(
					"Cannot set usedAutoAssociations to " + usedAutoAssociations);
		}
		state().setUsedAutomaticAssociations(usedAutoAssociations);
	}

	public int getNumContractKvPairs() {
		return state().getNumContractKvPairs();
	}

	public void setNumContractKvPairs(final int numContractKvPairs) {
		/* The MerkleAccountState will throw a MutabilityException if this MerkleAccount is immutable */
		state().setNumContractKvPairs(numContractKvPairs);
	}

//	public ContractKey getFirstContractStorageKey() {
//		return state().getFirstContractStorageKey();
//	}

	public int[] getFirstUint256Key() {
		return state().getFirstUint256Key();
	}

	public void setFirstUint256StorageKey(final int[] firstUint256Key) {
		state().setFirstUint256Key(firstUint256Key);
	}

//	public Map<EntityNum, Long> getCryptoAllowances() {
//		return state().getCryptoAllowances();
//	}

//	public void setCryptoAllowances(final SortedMap<EntityNum, Long> cryptoAllowances) {
//		throwIfImmutable("Cannot change this account's crypto allowances if it's immutable.");
//		state().setCryptoAllowances(cryptoAllowances);
//	}

//	public Map<EntityNum, Long> getCryptoAllowancesUnsafe() {
//		return state().getCryptoAllowancesUnsafe();
//	}

//	public void setCryptoAllowancesUnsafe(final Map<EntityNum, Long> cryptoAllowances) {
//		throwIfImmutable("Cannot change this account's crypto allowances if it's immutable.");
//		state().setCryptoAllowancesUnsafe(cryptoAllowances);
//	}

	public Set<FcTokenAllowanceId> getApproveForAllNfts() {
		return state().getApproveForAllNfts();
	}

	public void setApproveForAllNfts(final Set<FcTokenAllowanceId> approveForAllNfts) {
		throwIfImmutable(
				"Cannot change this account's approved for all NFTs allowances if it's immutable.");
		state().setApproveForAllNfts(approveForAllNfts);
	}

	public Set<FcTokenAllowanceId> getApproveForAllNftsUnsafe() {
		return state().getApproveForAllNftsUnsafe();
	}

	public Map<FcTokenAllowanceId, Long> getFungibleTokenAllowances() {
		return state().getFungibleTokenAllowances();
	}

	public void setFungibleTokenAllowances(
			final SortedMap<FcTokenAllowanceId, Long> fungibleTokenAllowances) {
		throwIfImmutable(
				"Cannot change this account's fungible token allowances if it's immutable.");
		state().setFungibleTokenAllowances(fungibleTokenAllowances);
	}

	public Map<FcTokenAllowanceId, Long> getFungibleTokenAllowancesUnsafe() {
		return state().getFungibleTokenAllowancesUnsafe();
	}

	public void setFungibleTokenAllowancesUnsafe(
			final Map<FcTokenAllowanceId, Long> fungibleTokenAllowances) {
		throwIfImmutable(
				"Cannot change this account's fungible token allowances if it's immutable.");
		state().setFungibleTokenAllowancesUnsafe(fungibleTokenAllowances);
	}

	public boolean isDeclinedReward() {
		return state().isDeclineReward();
	}

	public void setDeclineReward(boolean declineReward) {
		throwIfImmutable("Cannot change this account's declineReward if it's immutable");
		state().setDeclineReward(declineReward);
	}

	public boolean hasBeenRewardedSinceLastStakeMetaChange() {
		return state().getStakeAtStartOfLastRewardedPeriod() != -1L;
	}

	public long totalStakeAtStartOfLastRewardedPeriod() {
		return state().getStakeAtStartOfLastRewardedPeriod();
	}

	public void setStakeAtStartOfLastRewardedPeriod(final long balanceAtStartOfLastRewardedPeriod) {
		throwIfImmutable(
				"Cannot change this account's balanceAtStartOfLastRewardedPeriod if it's"
						+ " immutable");
		state().setStakeAtStartOfLastRewardedPeriod(balanceAtStartOfLastRewardedPeriod);
	}

	public long getStakedToMe() {
		return state().getStakedToMe();
	}

	public void setStakedToMe(long stakedToMe) {
		throwIfImmutable("Cannot change this account's stakedToMe if it's immutable");
		state().setStakedToMe(stakedToMe);
	}

	public long totalStake() {
		return state().balance() + state().getStakedToMe();
	}

	public long getStakePeriodStart() {
		return state().getStakePeriodStart();
	}

	public void setStakePeriodStart(final long stakePeriodStart) {
		throwIfImmutable("Cannot change this account's stakePeriodStart if it's immutable");
		state().setStakePeriodStart(stakePeriodStart);
	}

	/**
	 * Get the num [of shard.realm.num] of node/account this account has staked its hbar to If the
	 * returned value is negative it is staked to a node and node num is the absolute value of
	 * (-stakedNum - 1) If the returned value is positive it is staked to an account and the
	 * accountNum is stakedNum.
	 *
	 * @return num [of shard.realm.num] of node/account
	 */
	public long getStakedId() {
		return state().getStakedNum();
	}

	/**
	 * Sets the id of account or node to which this account is staking its hbar to. If stakedId &lt;
	 * 0 it will be a node id and if stakedId &gt; 0 it is an account number.
	 *
	 * @param stakedId The node num of the node
	 */
	public void setStakedId(long stakedId) {
		throwIfImmutable("Cannot change this account's staked id if it's immutable");
		state().setStakedNum(stakedId);
	}

	public boolean mayHavePendingReward() {
		return getStakedId() < 0 && !isDeclinedReward();
	}

	public long getStakedNodeAddressBookId() {
		if (state().getStakedNum() >= 0) {
			throw new IllegalStateException("Account is not staked to a node");
		}
		return -state().getStakedNum() - 1;
	}

//	public Iterator<ExpirableTxnRecord> recordIterator() {
//		return records().iterator();
//	}
//
//	public int numRecords() {
//		return records().size();
//	}

	public boolean hasAlias() {
		return getAlias().length > 0;
	}

	public boolean hasAutoRenewAccount() {
		return state().hasAutoRenewAccount();
	}

	public long getAutoRenewAccount() {
		return state().getAutoRenewAccount();
	}

	public void setAutoRenewAccount(final long autoRenewAccount) {
		throwIfImmutable("Cannot change this account's autoRenewAccount if it's immutable.");
		state().setAutoRenewAccount(autoRenewAccount);
	}
}