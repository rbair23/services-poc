module com.hedera.hashgraph.api {
	requires com.hedera.hashgraph.protoparse;
	requires com.hedera.hashgraph.hapi;
	requires com.swirlds.common;
	requires com.swirlds.virtualmap;
	requires com.swirlds.jasperdb;
	requires static com.github.spotbugs.annotations;
	exports com.hedera.hashgraph.base;
    exports com.hedera.hashgraph.base.state;
    exports com.hedera.hashgraph.base.record;
}