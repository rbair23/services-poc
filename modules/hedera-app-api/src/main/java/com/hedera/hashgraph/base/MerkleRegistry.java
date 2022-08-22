package com.hedera.hashgraph.base;

import com.swirlds.common.merkle.MerkleNode;

import java.util.function.UnaryOperator;

/**
 * Defines a registry of merkle nodes.
 * <p>
 * Individual service modules need some backing in the shared merkle tree state for storing their own
 * persistent state. The merkle tree state is owned by the platform modules, with an implementation of
 * {@code SwirldState} being constructed by the application module. The application module implements
 * this interface, and gives this interface to service modules during their construction. Each
 * service module can look up its own state in the registry and, if that state is not present in the
 * register, create and register it.
 * <p>
 * It is also possible for the service module to replace the state it had previously registered.
 * This is useful during migration at startup.
 */
public interface MerkleRegistry {
	/**
	 * Looks up a merkle node in the backing merkle tree associated with the given
	 * {@code id}. If not found, the supplied callback function is invoked, giving
	 * the application a chance to create a new node associated with that id. The
	 * returned value from the callback function will be returned by this function.
	 * <p>
	 * The input to the callback function is the merkle node, if any, already registered
	 * with the specified {@code id}.
	 * <p>
	 * If the callback function returns the same merkle node passed to it (identity),
	 * then no change is made to the underlying merkle tree. If the callback function
	 * returns a different merkle node, then the old one will be replaced with the new
	 * one in the underlying merkle tree (this supports the migration use case). If
	 * the callback function returns null, then the merkle node will be removed from
	 * the backing merkle tree.
     *
	 * @param id id of the merkle node to register or lookup in the backing merkle tree.
	 *           This ID must be unique across all modules, and must not be null.
	 * @param callback A callback function that will always be called.
	 *                 This function is responsible for creating the merkle node if
	 *                 it does not exist, or replacing it during migration. It cannot be null.
	 * @return the merkle node returned from the callback function, which is also
	 *         (at the conclusion of this call) registered in the backing merkle tree.
	 * @throws NullPointerException if the specified id is null, or the callback is null
	 */
	<T extends MerkleNode> T getOrRegister(String id, UnaryOperator<T> callback);
}
