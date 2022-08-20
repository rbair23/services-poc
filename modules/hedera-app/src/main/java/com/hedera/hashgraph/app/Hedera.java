package com.hedera.hashgraph.app;

import com.hedera.hashgraph.app.grpc.GrpcHandler;
import com.hedera.hashgraph.app.throttle.ThrottleAccumulatorImpl;
import com.hedera.hashgraph.app.workflows.ingest.IngestCheckerImpl;
import com.hedera.hashgraph.base.ThrottleAccumulator;
import com.hedera.hashgraph.token.AccountService;
import com.hedera.hashgraph.token.impl.AccountServiceImpl;
import com.swirlds.common.system.Platform;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;

public class Hedera {
	public static void main(String[] args) {
		// Create all the services
		final AccountService accountService = new AccountServiceImpl(null);

		// Create various helper classes
		final ThrottleAccumulator throttleAccumulator = new ThrottleAccumulatorImpl();

		// Create and initialize the platform
		final Platform platform = new FakePlatform();

		// Create the different workflows
		final var ingestChecker = new IngestCheckerImpl(throttleAccumulator);

		// Now for each service, hook it up to the gRPC server! Yay.
		// (We could do a similar block for support REST or gRPC Web)
		final var handler = new GrpcHandler(platform, accountService, ingestChecker);
		final var routing = GrpcRouting.builder()
				.register(handler.service("proto.FileService")
						.transaction("createFile")
						.build())
				.build();

		// Now that the server has been configured, go ahead and start it :-)
		final var server = GrpcServer.create(routing);
		server.start();
	}
}
