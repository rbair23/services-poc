package com.hedera.hashgraph.app.merkle;

import com.hedera.hashgraph.base.MerkleRegistry;
import com.swirlds.common.merkle.MerkleNode;

import java.util.function.UnaryOperator;

public class MerkleRegistryImpl implements MerkleRegistry {
	@Override
	public <T extends MerkleNode> T getOrRegister(final String id, final UnaryOperator<T> callback) {
		return null;
	}
}
