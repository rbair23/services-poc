package com.hedera.hashgraph.app.state;

import com.swirlds.common.merkle.MerkleInternal;
import com.swirlds.common.merkle.MerkleNode;
import com.swirlds.common.merkle.impl.PartialNaryMerkleInternal;
import com.swirlds.merkle.map.MerkleMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An internal merkle node acting as the root for all state related to a particular service module.
 * Each service module has its own namespace within which all merkle state is stored (similar to
 * how each microservice in a system may have a different namespace or schema within the database).
 */
public final class ServiceStateNode extends PartialNaryMerkleInternal implements MerkleInternal {
    private static final long CLASS_ID = 2202034923L;
    private static final int VERSION_0 = 0;
    private static final int CURRENT_VERSION = VERSION_0;

    /**
     * Standardized child index at which to find the "serviceName" of the service for which
     * this node exists.
     */
    private static final int NAME_CHILD_INDEX = 0;

    /**
     * @deprecated DO NOT CALL THIS CONSTRUCTOR. It exists only for deserialization.
     */
    @Deprecated(since = "1.0")
    ServiceStateNode() {
    }

    /**
     * Create a new ServiceStateNode.
     *
     * @param serviceName The name of the service for which this node holds state.
     */
    public ServiceStateNode(@Nonnull String serviceName) {
        setChild(NAME_CHILD_INDEX, new StringLeaf(serviceName));
    }

    /**
     * Create a fast copy.
     *
     * @param from The node to copy from
     */
    private ServiceStateNode(@Nonnull ServiceStateNode from) {
        final var numChildren = getNumberOfChildren();
        for (int i = 0; i < numChildren; i++) {
            setChild(i, getChild(i).copy());
        }
    }

    /**
     * Gets the name of the service associated with this state.
     * @throws IllegalStateException If, somehow, the service name was not set.
     * @return The name of the service. This will never be null.
     */
    @Nonnull
    public String getServiceName() {
        // It should not be possible for a ServiceStateNode to exist without the "name". It could
        // only happen if the default constructor were used, when it shouldn't have been.
        final StringLeaf nameLeaf = getChild(NAME_CHILD_INDEX);
        if (nameLeaf == null) {
            throw new IllegalStateException("Unexpectedly, the service name is null!");
        }

        return nameLeaf.getValue();
    }

    /**
     * Given some state key, look for an associated piece of state (a {@link MerkleNode} of some kind)
     * and return it. If it cannot be found, return null.
     *
     * @param stateKey The key of the state to lookup. Cannot be null.
     * @return The {@link MerkleNode}, if one was found, that corresponds with the state key. Otherwise, null.
     */
    @Nullable
    MerkleNode find(@Nonnull String stateKey) {
        Objects.requireNonNull(stateKey);
        final int indexOfNode = indexOf(stateKey);
        return indexOfNode == -1 ? null : getChild(indexOfNode);
    }

    /**
     * Idempotent "put" of the given merkle node into the tree. If there is already a merkle node
     * at this state key location, it will be replaced.
     *
     * @param stateKey The state key. Cannot be null.
     * @param merkle The merkle node to set. Cannot be null.
     */
    public void put(@Nonnull String stateKey, @Nonnull MerkleNode merkle) {
        final int existingIndex = indexOf(stateKey);
        final int index = existingIndex == -1 ? getNumberOfChildren() : existingIndex;
        setChild(index, new StringLeaf(stateKey));
        setChild(index + 1, merkle);
    }

    /**
     * If there is state with the associated state key, removes it.
     *
     * @param stateKey The state key. Cannot be null.
     */
    public void remove(@Nonnull String stateKey) {
        Objects.requireNonNull(stateKey);

        final int matchingIndex = indexOf(stateKey);
        if (matchingIndex != -1) {
            final var lastChild = getNumberOfChildren() - 2;
            for (int i = matchingIndex - 1; i < lastChild; i++) {
                setChild(i, getChild(i + 2));
            }
        }
    }

    /**
     * Get the index of the state associated with the given state key, or null if there is no
     * such state.
     *
     * @param stateKey The state key, cannot be null
     * @return The index of the associated state, or -1 if there is not any
     */
    private int indexOf(@Nonnull String stateKey) {
        Objects.requireNonNull(stateKey);

        // The first child of ServiceStateNode is a StringLeaf containing the serviceName. The
        // subsequent children come in pairs -- the first is a StringLeaf with the stateKey,
        // the next is the corresponding MerkleNode.
        final var numChildren = getNumberOfChildren();
        for (int i = 1; i < numChildren - 1; i +=2 ) {
            final StringLeaf idNode = getChild(i);
            if (stateKey.equals(idNode.getValue())) {
                return i + 1;
            }
        }

        return -1;
    }

    @Override
    public long getClassId() {
        return CLASS_ID;
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public ServiceStateNode copy() {
        return new ServiceStateNode(this);
    }
}