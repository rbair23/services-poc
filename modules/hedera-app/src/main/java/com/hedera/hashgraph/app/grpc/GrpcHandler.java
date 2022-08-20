package com.hedera.hashgraph.app.grpc;

import com.hedera.hashgraph.app.workflows.ingest.IngestChecker;
import com.hedera.hashgraph.hapi.model.TransactionResponse;
import com.hedera.hashgraph.hapi.model.base.Transaction;
import com.hedera.hashgraph.token.AccountService;
import com.swirlds.common.system.Platform;

/**
 * Common handler for <b>ALL</b> gRPC requests.
 *
 * <p>In our system, all gRPC requests are one of two types. They are either transactions, or queries. All
 * transactions send us a protobuf object of type {@link Transaction}, and their response is always the generic
 * {@link TransactionResponse}. Queries are similar. This allows us to have a single generic handler for all
 * gRPC requests.
 *
 * <p>A single GrpcHandler instance can be used to create service descriptions for all services and methods.
 * There is no reason to create more than one instance of this per node instance.
 */
public final class GrpcHandler {
	/**
	 * A single implementation of UnaryMethod that handles all transaction ingest calls.
	 */
	private final TransactionMethod transactionMethod;

	/**
	 * Create a new GrpcHandler.
	 *
	 * @param platform A reference to the Platform. Cannot be null.
	 * @param accountService A reference to the AccountService to use for looking up account keys and balances.
	 *                       Cannot be null.
	 * @param ingestChecker Used to validate different aspects of the ingestion flow. Cannot be null.
	 */
	public GrpcHandler(Platform platform, AccountService accountService, IngestChecker ingestChecker) {
		this.transactionMethod = new TransactionMethod(
				new GrpcIngestPipeline(platform, accountService, ingestChecker));
	}

	/**
	 * Create and return a new {@link ServiceBuilder}, used for building a gRPC service definition.
	 *
	 * @param serviceName The name of the service. Cannot be null or empty or blank.
	 * @return A service builder. Will never be null. Creates a new instance on each call.
	 */
	public ServiceBuilder service(String serviceName) {
		return new ServiceBuilder(serviceName, transactionMethod);
	}
}
