module com.hedera.hashgraph.token.impl {
	requires com.hedera.hashgraph.token;
	requires com.hedera.hashgraph.api;
	requires com.hedera.hashgraph.hapi;
	requires com.swirlds.common;
	requires com.swirlds.merkle;
	requires com.hedera.hashgraph.protoparse;
	requires static com.github.spotbugs.annotations;
	exports com.hedera.hashgraph.token.impl;
}