package blar;

import com.hederahashgraph.api.proto.java.*;
import com.hederahashgraph.service.proto.java.FileServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SimpleTest {
    @Test
    void test() {
        final var transactionBody = TransactionBody.newBuilder()
                .setFileCreate(FileCreateTransactionBody.newBuilder())
                .setTransactionID(TransactionID.newBuilder()
                        .setAccountID(AccountID.newBuilder()
                                .setShardNum(0)
                                .setRealmNum(0)
                                .setAccountNum(1000)
                                .build())
                        .setTransactionValidStart(Timestamp.newBuilder()
                                .setSeconds((System.currentTimeMillis() / 1000) + 1000)
                                .build())
                        .build())
                .build();

        final var signedTransaction = SignedTransaction.newBuilder()
                .setBodyBytes(transactionBody.toByteString())
                .build();

        final var transaction = Transaction.newBuilder()
                .setSignedTransactionBytes(signedTransaction.toByteString())
                .build();

        final var channel = ManagedChannelBuilder.forAddress("localhost", 1408)
                .usePlaintext()
                .build();
        final var blockingStub = FileServiceGrpc.newBlockingStub(channel);
//        final var response = blockingStub.createFile(transaction);
//        assertNotNull(response);
    }
}
