package com.hedera.hashgraph.app;

import com.hedera.hashgraph.app.grpc.GrpcHandler;
import com.hedera.hashgraph.app.workflows.ingest.TransactionIngestWorkflow;
import com.hedera.hashgraph.file.FileService;
import com.hedera.hashgraph.file.impl.FileServiceImpl;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;

public class Hedera {
	public static void main(String[] args) {
		// Now for each service, hook it up to the gRPC server! Yay.
		final var txIngestFlow = new TransactionIngestWorkflow();
		final var handler = new GrpcHandler(txIngestFlow);
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
