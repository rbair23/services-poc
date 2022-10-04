package com.hedera.hashgraph.token.impl.store;

import com.hedera.hashgraph.base.state.State;
import com.hedera.hashgraph.base.state.StateRegistry;
import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.AccountID;
import com.hedera.hashgraph.hapi.model.Key;
import com.hedera.hashgraph.hapi.schema.AccountIDSchema;
import com.hedera.hashgraph.hapi.schema.KeySchema;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.impl.entity.AccountImpl;
import com.hedera.hashgraph.token.impl.store.merkle.MerkleAccount;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

/**
 * Provides methods for interacting with the underlying data storage mechanisms for working with Accounts.
 * <p>
 * This class is not exported from the module. It is an internal implementation detail.
 */
public class AccountStore {
	/**
	 * The ID for representing the account state stored in the {@link StateRegistry}.
	 */
	private static final String ACCOUNT_STORE = "ACCOUNT_STORE";

	/**
	 * The underlying data storage class that holds the account data.
	 */
	private final State<Long, MerkleAccount> accountState;

	/**
	 * Create a new {@link AccountStore} instance.
	 *
	 * @param states The state to use.
	 */
	public AccountStore(@NonNull States states) {
		this.accountState = states.get(ACCOUNT_STORE);
		assert this.accountState != null : "States must throw IAE if not found, and not return null";
	}

	/**
	 * Called to register the account store state with the {@link StateRegistry}.
	 *
	 * @param registry The registry where we save states.
	 */
	public static void register(@NonNull StateRegistry registry) {
		registry.<Long, MerkleAccount>getOrRegister(ACCOUNT_STORE, (builder, existing) -> {
			if (existing.isEmpty()) {
				return builder.<Long, MerkleAccount>inMemory(ACCOUNT_STORE).build();
			} else {
				// If there was anything to migrate, I'd do it here
				return existing.get();
			}
		});
	}

	/**
	 * Creates a new {@link Account}. The created account <strong>is not</strong> persisted. To persist
	 * the state, call {@link #saveAccount(Account)}.
	 *
	 * @param accountNum The account number
	 * @param key The key to use. This cannot be null
	 * @param initialBalance The initial account balance
	 * @param receiverSigRequired Whether a receiver sig is required
	 * @param autoRenewPeriod The automatic renewal period
	 * @param memo A memo. Cannot be null.
	 * @param maxAutomaticTokenAssociations max automatic token associations
	 * @param stakedId The staking node id
	 * @param declineReward Whether the user wants to decline rewards
	 * @return The created account.
	 */
	public @NonNull Account createAccount(
			long accountNum,
			@NonNull Key key,
			long initialBalance,
			boolean receiverSigRequired,
			long autoRenewPeriod,
			@NonNull String memo,
			int maxAutomaticTokenAssociations,
			long stakedId,
			boolean declineReward
	) {
		return new AccountImpl(
				accountNum,
				Optional.empty(),
				Optional.of(null), // TODO How to convert from "key" to bytes?
				-1,
				initialBalance,
				memo,
				false,
				false,
				receiverSigRequired,
				-1,
				0,
				maxAutomaticTokenAssociations,
				0,
				0,
				0,
				0,
				0,
				0,
				stakedId,
				declineReward,
				0,
				0);
	}

	public Optional<Account> getAccount(AccountID id) {
		// TODO deal with alias!!
		final var opt = accountState.get(id.accountNum().get());
		if (opt.isPresent()) {
			final var merkleAccount = opt.get();
			return Optional.of(new AccountImpl(
					id.accountNum().get(),
					Optional.empty(),
					Optional.empty(),
					1000,
					merkleAccount.getBalance(),
					merkleAccount.getMemo(),
					false,
					false,
					false,
					-1,
					0,
					0,
					0,
					0,
					0,
					0,
					0,
					0,
					0,
					false,
					0,
					0));
		} else {
			return Optional.empty();
		}
	}

	// What to return if it isn't there?
	public long getAccountBalance(AccountID id) {
		final var opt = accountState.get((Long) id.account().value());
		return opt.map(MerkleAccount::getBalance).orElse(0L);
	}

	// Maybe the account already exists (i.e. I'm replacing it), or maybe it is brand new.
	public void saveAccount(Account account) {
		// Look up the leaf based on the account number, in a read/write way
		final var opt = accountState.getForModify(account.accountNumber());
		final var merkle = opt.orElseGet(MerkleAccount::new);

		// TODO Here we would just "slam" all the state from account into merkle. If we kept track of
		//      what the diff was between them, we could just set the changes. But that effort may be
		//      more than just overwriting everything in the leaf.
		merkle.setBalance(account.balance());
//		merkle.setAccountKey(account.key());
//		merkle.setAlias(account.alias());
//		merkle.setApproveForAllNfts(null);
		merkle.setAutoRenewAccount(account.autoRenewAccountNumber());
		merkle.setDeleted(false);
//		merkle.setAutoRenewSecs();
		merkle.setDeclineReward(account.declineReward());
		// etc etc

		// We only need to do this step if we didn't get anything back from getForModify
		if (opt.isEmpty()) {
			accountState.put(account.accountNumber(), merkle);
		}
	}

	private MerkleAccount getAccountLeaf(long accountNumber) {
		// TODO if there is no such account, throw an exception?
		final var opt = accountState.get(accountNumber);
		final var account = opt.get();
		return account;
	}

	private AccountID createAccountID(long accountNum, byte[] alias) {
		return alias == null || alias.length == 0
				? new AccountID(0, 0, new OneOf<>(AccountID.AccountOneOfType.ACCOUNT_NUM, accountNum))
				: new AccountID(0, 0, new OneOf<>(AccountID.AccountOneOfType.ALIAS, alias));
	}

	private Key createKeyFromBytes(byte[] bytes) {
		return new Key(new OneOf<>(Key.KeyOneOfType.ED25519, bytes));
	}
}
