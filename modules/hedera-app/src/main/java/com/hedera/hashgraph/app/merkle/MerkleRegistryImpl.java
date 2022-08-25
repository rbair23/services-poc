package com.hedera.hashgraph.app.merkle;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.swirlds.common.merkle.MerkleNode;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.function.UnaryOperator;

/**
 * An implementation of the {@link MerkleRegistry} based on {@link com.swirlds.common.system.SwirldState}.
 * The Hedera application maintains the top part of the merkle tree associated with the
 * {@link com.swirlds.common.system.SwirldState}. Whenever a service module needs to "attach" a merkle node
 * to this state, or migrate it, or detach it, they do so by calling this registry method and taking different
 * actions in the callback.
 *
 * @see MerkleRegistry
 */
@NotThreadSafe
public class MerkleRegistryImpl implements MerkleRegistry {
	// TODO Create a constructor that takes a ServicesState and implement the getOrRegister method.

	@Override
	public <T extends MerkleNode> T getOrRegister(final String id, final UnaryOperator<T> callback) {
		return null;
	}
}
