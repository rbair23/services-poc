package com.hedera.hashgraph.app.state;

import com.hedera.hashgraph.base.state.States;
import com.swirlds.common.merkle.MerkleInternal;
import com.swirlds.common.merkle.impl.PartialNaryMerkleInternal;
import com.swirlds.common.system.SwirldDualState;
import com.swirlds.common.system.SwirldState;
import com.swirlds.common.system.address.AddressBook;
import com.swirlds.common.system.transaction.SwirldTransaction;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * The root of all merkle state for the Hedera application.
 */
public final class HederaStateImpl extends PartialNaryMerkleInternal
        implements MerkleInternal, SwirldState.SwirldState2, HederaState {

    // For serialization
    private static final long CLASS_ID = 29399209029302L;
    private static final int VERSION_0 = 0;
    private static final int CURRENT_VERSION = VERSION_0;

    /**
     * Create a new HederaState! This can be called either explicitly or as part of saved state loading.
     */
    public HederaStateImpl() {
    }

    /**
     * Private constructor for fast-copy.
     *
     * @param that The other state to fast-copy from. Cannot be null.
     */
    private HederaStateImpl(@Nonnull HederaStateImpl that) {
        final int numChildren = that.getNumberOfChildren();
        for (int i = 0; i < numChildren; i++) {
            final var child = that.getChild(i).copy();
            setChild(i, child);
        }
    }

    /**
     * Adds the given {@link ServiceStateNode} to the state merkle tree. This call <strong>only</strong>
     * takes effect if there is not already a node with the same {@code serviceName} on the state.
     * Otherwise, the call is a no-op.
     *
     * @param node The node to add. Cannot be null.
     */
    public void addServiceStateNode(@Nonnull ServiceStateNode node) {
        // See if there is already a node for this, if not, add it.
        final var optNode = getServiceStateNode(node.getServiceName());
        if (optNode.isEmpty()) {
            // Didn't find it, so we will add a new one
            setChild(getNumberOfChildren(), node);
        }
    }

    /**
     * Finds and returns the {@link ServiceStateNode} with a matching {@code serviceName}, if there
     * is one.
     * @param serviceName The service name. Cannot be null.
     * @return An {@link Optional} that is empty if nothing was found, or it contains the matching
     *         {@link ServiceStateNode}.
     */
    @Nonnull
    public Optional<ServiceStateNode> getServiceStateNode(@Nonnull String serviceName) {
        Objects.requireNonNull(serviceName);

        // Find a node with this service name, if there is one.
        final int numNodes = getNumberOfChildren();
        for (int i = 0; i < numNodes; i++) {
            final ServiceStateNode node = getChild(i);
            if (serviceName.equals(node.getServiceName())) {
                // We found a match, so we'll avoid adding it
                return Optional.of(node);
            }
        }

        return Optional.empty();
    }


    /**
     * Gets a {@link States} instance with access to all state in this registry.
     *
     * @return A non-null States instance with access to all state in this registry.
     */
    @Override
    public StatesImpl createStates(String serviceName) {
        final var opt = getServiceStateNode(serviceName);
        final var serviceRoot = opt.orElseThrow();
        return new StatesImpl(serviceRoot);
    }


    // TODO, actually stored in MerkleNetworkContext, a child of HederaStateImpl
    private long nextEntityId = 1000;

    @Override
    public long getNextEntityId() {
        return nextEntityId;
    }

    @Override
    public void setNextEntityId(long nextEntityId) {
        this.nextEntityId = nextEntityId;
    }


    // STANDARD API FOR SWIRLDS_STATE_2

    @Override
    public AddressBook getAddressBookCopy() {
        return null;
    }

    @Override
    public void handleTransaction(long l, boolean b, Instant instant, Instant instant1, SwirldTransaction swirldTransaction, SwirldDualState swirldDualState) {

    }

    @Override
    public void expandSignatures(SwirldTransaction swirldTransaction) {

    }

    @Override
    public HederaStateImpl copy() {
        return new HederaStateImpl(this);
    }

    @Override
    public long getClassId() {
        return CLASS_ID;
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }
}