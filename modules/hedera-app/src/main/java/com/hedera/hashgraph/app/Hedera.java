package com.hedera.hashgraph.app;

import com.hedera.hashgraph.app.fee.FeeAccumulatorImpl;
import com.hedera.hashgraph.app.fee.FeeScheduleLookup;
import com.hedera.hashgraph.app.grpc.HederaGrpcHandler;
import com.hedera.hashgraph.app.record.RecordStreamManager;
import com.hedera.hashgraph.app.state.HederaState;
import com.hedera.hashgraph.app.state.HederaStateImpl;
import com.hedera.hashgraph.app.state.ServiceStateNode;
import com.hedera.hashgraph.app.state.StateRegistryImpl;
import com.hedera.hashgraph.app.throttle.ThrottleAccumulatorImpl;
import com.hedera.hashgraph.app.workflows.handle.HandleTransactionDispatcherImpl;
import com.hedera.hashgraph.app.workflows.handle.HandleTransactionWorkflow;
import com.hedera.hashgraph.app.workflows.ingest.IngestCheckerImpl;
import com.hedera.hashgraph.app.workflows.prehandle.PreHandleDispatcherImpl;
import com.hedera.hashgraph.app.workflows.prehandle.PreHandleWorkflow;
import com.hedera.hashgraph.base.ThrottleAccumulator;
import com.hedera.hashgraph.file.impl.FileServiceImpl;
import com.hedera.hashgraph.hapi.model.FeeSchedule;
import com.hedera.hashgraph.hapi.model.Key;
import com.hedera.hashgraph.hapi.model.TransactionFeeSchedule;
import com.hedera.hashgraph.hapi.model.base.TimestampSeconds;
import com.hedera.hashgraph.token.entity.Account;
import com.hedera.hashgraph.token.entity.AccountBuilder;
import com.hedera.hashgraph.token.impl.CryptoServiceImpl;
import com.hedera.hashgraph.token.impl.TokenServiceImpl;
import com.swirlds.common.system.Platform;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

public class Hedera {
    public static final String CRYPTO_SERVICE = "CryptoService";
    public static final String FILE_SERVICE = "FileService";
    public static final String TOKEN_SERVICE = "TokenService";

    private final Platform platform;
    private final HederaState hederaState;
    private final ServicesAccessor servicesAccessor;

    /**
     * Create a new Hedera application
     */
    public Hedera(Platform platform) {
        // Create the state. It will be empty by default. In reality this should be created via
        // a callback from the platform. The Platform may load a state from saved state, or it
        // may create a new one, so I shouldn't be creating it manually here.
        final var state = new HederaStateImpl();
        state.addServiceStateNode(new ServiceStateNode(CRYPTO_SERVICE));
        state.addServiceStateNode(new ServiceStateNode(FILE_SERVICE));
        state.addServiceStateNode(new ServiceStateNode(TOKEN_SERVICE));

        // Save this for later use
        hederaState = state;

        // Create the platform. In reality this is created indirectly via Browser, although at
        // some point we hope to be able to create the platform directly.
        this.platform = platform;
        final var cryptoMerkleRegistry = new StateRegistryImpl(state.getServiceStateNode(CRYPTO_SERVICE).orElseThrow());
        final var fileMerkleRegistry = new StateRegistryImpl(state.getServiceStateNode(FILE_SERVICE).orElseThrow());
        final var tokenMerkleRegistry = new StateRegistryImpl(state.getServiceStateNode(TOKEN_SERVICE).orElseThrow());

        // Create all the services and add them to the accessor, so we have an easy way to refer to them later
        servicesAccessor = new ServicesAccessor(
                new ServicesContext<>(new CryptoServiceImpl(cryptoMerkleRegistry), cryptoMerkleRegistry),
                new ServicesContext<>(new FileServiceImpl(fileMerkleRegistry), fileMerkleRegistry),
                new ServicesContext<>(new TokenServiceImpl(tokenMerkleRegistry), tokenMerkleRegistry));

        // In reality, we wouldn't always do a genesis flow, but for now for a POC it is OK to assume this.
        genesis(servicesAccessor);
    }

    /**
     * Start the Hedera server, listening on all configured endpoints.
     */
    public void start() {
        // Create various helper classes
        final ThrottleAccumulator throttleAccumulator = new ThrottleAccumulatorImpl();

        // Create the different workflows
        final var ingestChecker = new IngestCheckerImpl(throttleAccumulator);

        // Get a component for looking up the fee schedule
        // TODO What happens when the fee schedule changes dynamically? Then what?
        final var feeScheduleLookup = new FeeScheduleLookup(
                new FeeSchedule(
                        List.of(new TransactionFeeSchedule.Builder().build()),
                        new TimestampSeconds(100)));

        // Start up the platform
        final var preHandleExecutor = Executors.newFixedThreadPool(5);
        final var preHandleDispatcher = new PreHandleDispatcherImpl(servicesAccessor);
        final var preHandleWorkflow = new PreHandleWorkflow(
                preHandleExecutor, () -> servicesAccessor.cryptoService().service().createQueryHandler(
                        hederaState.createStates(CRYPTO_SERVICE)), ingestChecker, preHandleDispatcher);

        final var recordStreamManager = new RecordStreamManager();
        final var handleTransactionDispatcher = new HandleTransactionDispatcherImpl(servicesAccessor);
        final var handleTransactionWorkflow = new HandleTransactionWorkflow(
                () -> hederaState, // TODO In reality, this should be the working state. Right now I just have the one state, which isn't right.
                servicesAccessor.cryptoService().service(),
                handleTransactionDispatcher,
                throttleAccumulator,
                recordStreamManager,
                () -> new FeeAccumulatorImpl(feeScheduleLookup)); // TODO Gotta be sure to pick up the latest feeScheduleLookup

        ((FakePlatform)platform).start(preHandleWorkflow, handleTransactionWorkflow);

        // Now for each service, hook it up to the gRPC server! Yay.
        // (We could do a similar block for support REST or gRPC Web)
        final var handler = new HederaGrpcHandler(platform, servicesAccessor.cryptoService().service(), ingestChecker);
//		final var routing = GrpcRouting.builder()
//				.register(handler.service("proto.FileService")
//						.transaction("createFile")
//						.build())
//				.build();

        // Now that the server has been configured, go ahead and start it :-)
//		final var server = GrpcServer.create(routing);
//		server.start();
    }

    /**
     * This should only be used if we are not starting from a saved state. Right now there
     * is no guard for this. But if we are starting from genesis it creates default accounts
     * and such.
     */
    private void genesis(ServicesAccessor sa) {
        // In this example, the only thing I'm creating at genesis is account 0.0.2. In real life we'd
        // create a whole pile of additional stuff. Notice how there is a special method on the crypto service
        // transaction handler for creating genesis accounts. It will basically just ram the account into state
        // without charging anybody or creating a record. Maybe that isn't right, and it should create a record...
        final var states = hederaState.createStates(CRYPTO_SERVICE);
        final var txHandler = sa.cryptoService().service().createTransactionHandler(states);
        txHandler.createGenesisAccount(new Account() {
            @Override
            public long shardNumber() {
                return 0;
            }

            @Override
            public long realmNumber() {
                return 0;
            }

            @Override
            public long accountNumber() {
                return 2;
            }

            @Override
            public Optional<byte[]> alias() {
                return Optional.empty();
            }

            @Override
            public boolean isHollow() {
                return false;
            }

            @Override
            public Optional<Key> key() {
                return Optional.empty();
            }

            @Override
            public long expiry() {
                return 0;
            }

            @Override
            public long balance() {
                return 50_000_000_000L;
            }

            @Override
            public String memo() {
                return "The genesis account";
            }

            @Override
            public boolean isDeleted() {
                return false;
            }

            @Override
            public boolean isSmartContract() {
                return false;
            }

            @Override
            public boolean isReceiverSigRequired() {
                return false;
            }

            @Override
            public long proxyAccountNumber() {
                return 0;
            }

            @Override
            public long numberOfOwnedNfts() {
                return 0;
            }

            @Override
            public int maxAutoAssociations() {
                return 0;
            }

            @Override
            public int usedAutoAssociations() {
                return 0;
            }

            @Override
            public int numAssociations() {
                return 0;
            }

            @Override
            public int numPositiveBalances() {
                return 0;
            }

            @Override
            public long ethereumNonce() {
                return 0;
            }

            @Override
            public long stakedToMe() {
                return 0;
            }

            @Override
            public long stakePeriodStart() {
                return 0;
            }

            @Override
            public long stakedNum() {
                return 0;
            }

            @Override
            public boolean declineReward() {
                return false;
            }

            @Override
            public long stakeAtStartOfLastRewardedPeriod() {
                return 0;
            }

            @Override
            public long autoRenewAccountNumber() {
                return 0;
            }

            @Override
            public AccountBuilder copy() {
                throw new UnsupportedOperationException();
            }
        });

        states.commit();
    }

    public static void main(String[] args) {
        new Hedera(new FakePlatform()).start();
    }
}
