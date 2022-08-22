package com.hedera.hashgraph.app;

import com.hedera.hashgraph.app.fee.FeeAccumulatorImpl;
import com.hedera.hashgraph.app.grpc.GrpcHandler;
import com.hedera.hashgraph.app.merkle.MerkleRegistryImpl;
import com.hedera.hashgraph.app.throttle.ThrottleAccumulatorImpl;
import com.hedera.hashgraph.app.workflows.handle.HandleTransactionDispatcherImpl;
import com.hedera.hashgraph.app.workflows.handle.HandleTransactionWorkflow;
import com.hedera.hashgraph.app.workflows.ingest.IngestCheckerImpl;
import com.hedera.hashgraph.app.workflows.prehandle.PreHandleDispatcherImpl;
import com.hedera.hashgraph.app.workflows.prehandle.PreHandleWorkflow;
import com.hedera.hashgraph.base.MerkleRegistry;
import com.hedera.hashgraph.base.ThrottleAccumulator;
import com.hedera.hashgraph.file.impl.FileServiceImpl;
import com.hedera.hashgraph.token.impl.AccountServiceImpl;
import com.hedera.hashgraph.token.impl.TokenServiceImpl;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;

import java.util.concurrent.Executors;

public class Hedera {
	public static void main(String[] args) {
		// Create and initialize the platform
		final FakePlatform platform = new FakePlatform();
		final MerkleRegistry merkleRegistry = new MerkleRegistryImpl();

		// Create all the services
		final var servicesAccessor = new ServicesAccessor(
				new AccountServiceImpl(merkleRegistry),
				new FileServiceImpl(merkleRegistry),
				new TokenServiceImpl(merkleRegistry));

		// Create various helper classes
		final ThrottleAccumulator throttleAccumulator = new ThrottleAccumulatorImpl();

		// Create the different workflows
		final var ingestChecker = new IngestCheckerImpl(throttleAccumulator);

		// Start up the platform
		final var preHandleExecutor = Executors.newFixedThreadPool(5);
		final var preHandleDispatcher = new PreHandleDispatcherImpl(servicesAccessor);
		final var preHandleWorkflow = new PreHandleWorkflow(
				preHandleExecutor, servicesAccessor.accountService(), ingestChecker, preHandleDispatcher);

		final var handleTransactionDispatcher = new HandleTransactionDispatcherImpl(servicesAccessor, throttleAccumulator, FeeAccumulatorImpl::new);
		final var handleTransactionWorkflow = new HandleTransactionWorkflow(handleTransactionDispatcher);

		platform.start(preHandleWorkflow, handleTransactionWorkflow);

		// Now for each service, hook it up to the gRPC server! Yay.
		// (We could do a similar block for support REST or gRPC Web)
		final var handler = new GrpcHandler(platform, servicesAccessor.accountService(), ingestChecker);
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
