package com.hedera.hashgraph.app;

import com.hedera.hashgraph.app.grpc.GrpcHandler;
import com.hedera.hashgraph.file.FileService;
import com.hedera.hashgraph.file.impl.FileServiceImpl;
import io.grpc.ServiceDescriptor;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;
//import io.grpc.ServerServiceDefinition;
//import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Hedera {
	public static void main(String[] args) throws IOException {
		// Get references to the services
		final FileService fileService = new FileServiceImpl(null);
		//...
		//...
		//...
		//...
		//...
		//...
		//...

//		NettyServerBuilder builder = NettyServerBuilder.forPort(50211);
//		builder.keepAliveTime(10, TimeUnit.SECONDS)
//				.permitKeepAliveTime(10, TimeUnit.SECONDS)
//				.keepAliveTimeout(3, TimeUnit.SECONDS)
//				.maxConnectionAge(15, TimeUnit.SECONDS)
//				.maxConnectionAgeGrace(5, TimeUnit.SECONDS)
//				.maxConnectionIdle(10, TimeUnit.SECONDS)
//				.maxConcurrentCallsPerConnection(10)
//				.flowControlWindow(10240)
//				.directExecutor();
//
//		// Now for each service, hook it up to netty! Yay.
		final var fileEndpoint = fileService.getEndpoint();
//		builder.addService(GrpcHandler.endpoint("proto.FileService")
//						.signedTransaction("createFile", fileEndpoint::handleFileCreateTransaction)
//						// ... add more here as needed, signedTransaction, transaction, query, etc
//						.build());
//
//		// And for each additional service, add some code here to wire it up
//
//		// Finally, start netty :)
//		final var nettyServer = builder.build();
//		nettyServer.start();


		final var routing = GrpcRouting.builder()
//				.register(GrpcHandler.endpoint("proto.FileService")
//						.signedTransaction("createFile", fileEndpoint::handleFileCreateTransaction)
//						.build())
				.build();

		final var server = GrpcServer.create(routing);
		server.start();


	}
}
