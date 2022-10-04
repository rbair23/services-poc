package com.hedera.hashgraph.app.state;

import com.hedera.hashgraph.base.state.State;
import com.hedera.hashgraph.base.state.StateRegistry;
import com.hedera.hashgraph.base.state.StateRegistryCallback;
import com.hedera.hashgraph.base.state.States;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;
import java.util.Optional;

/**
 * An implementation of the {@link StateRegistry} based on merkle tree. Each instance of this class
 * acts as a different namespace. A new instance should be provided to each service instance, thereby
 * ensuring that each has its own unique namespace and cannot collide (intentionally or accidentally)
 * with others.
 *
 * @see StateRegistry
 */
@NotThreadSafe
public final class StateRegistryImpl implements StateRegistry {
	/**
	 * The root node onto which all state for a service will be registered. This is not the root of the
	 * entire merkle tree, but it is the root of the tree for this namespace.
	 */
	private final ServiceStateNode serviceRoot;

	/**
	 * Create a new instance.
	 *
	 * @param serviceRoot The {@link ServiceStateNode} instance for this registry to use. Cannot be null.
	 */
	public StateRegistryImpl(@Nonnull ServiceStateNode serviceRoot) {
		this.serviceRoot = Objects.requireNonNull(serviceRoot);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> void getOrRegister(String stateKey, StateRegistryCallback<K, V> createOrMigrate) {
		// Look up the existing state
		final var states = new StatesImpl(serviceRoot);
		final Optional<State<K, V>> opt = states.findExistingState(stateKey);

		// Get the new state to use from the createOrMigrate lambda
		var newState = createOrMigrate.apply(new StateBuilderImpl(), opt);

		// Update the serviceRoot with whatever changes were made
		final var existingState = opt.orElse(null);
		if (existingState != newState) {
			if (newState == null) {
				assert stateKey.equals(existingState.getStateKey()) : "State keys do not match";
				// Existing state could not have been null, so we need to remove it
				serviceRoot.remove(existingState.getStateKey());
			} else {
				// Put the new state into the tree
				if (newState instanceof StateBase<K, V> stateBase) {
					serviceRoot.put(stateKey, stateBase.getMerkle());
				} else {
					throw new IllegalStateException("Unexpected state type! Should not be possible!");
				}
			}
		}
	}
}
