module com.hedera.hashgraph.file.impl {
	requires com.hedera.hashgraph.file;
	requires com.hedera.hashgraph.api;
	requires com.hedera.hashgraph.hapi;
	requires com.swirlds.common;
	requires com.swirlds.merkle;
	requires com.hedera.hashgraph.protoparse;
	requires static com.github.spotbugs.annotations;
	exports com.hedera.hashgraph.file.impl;
}