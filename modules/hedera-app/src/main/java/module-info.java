module com.hedera.hashgraph.app {
	requires com.hedera.hashgraph.api;
	requires com.hedera.hashgraph.hapi;
	requires com.hedera.hashgraph.file;
	requires com.hedera.hashgraph.file.impl;
	requires com.hedera.hashgraph.token;
	requires com.hedera.hashgraph.token.impl;
//	requires io.helidon.grpc.server;
//	requires io.helidon.grpc.core;
	requires com.swirlds.common;
	requires com.swirlds.merkle;
	requires com.swirlds.virtualmap;
	requires com.swirlds.jasperdb;
	requires jsr305;

	requires java.desktop; // Shouldn't need this, but Platform requires it for now...
	requires com.hedera.hashgraph.protoparse;

}