module com.hedera.hashgraph.hapi {
	exports com.hedera.hashgraph.hapi;
	exports com.hedera.hashgraph.hapi.model;
	exports com.hedera.hashgraph.hapi.model.base;
	exports com.hedera.hashgraph.hapi.model.consensus;
	exports com.hedera.hashgraph.hapi.model.contract;
	exports com.hedera.hashgraph.hapi.model.file;
	exports com.hedera.hashgraph.hapi.model.freeze;
	exports com.hedera.hashgraph.hapi.model.network;
	exports com.hedera.hashgraph.hapi.model.scheduled;
	exports com.hedera.hashgraph.hapi.model.token;
	exports com.hedera.hashgraph.hapi.model.util;
	exports com.hedera.hashgraph.hapi.proto.parsers;
	requires com.hedera.hashgraph.protoparse;
}