package blar;

import com.hedera.hashgraph.app.FakePlatform;
import com.hedera.hashgraph.app.Hedera;
import com.hedera.hashgraph.base.model.Utils;
import com.hedera.hashgraph.hapi.OneOf;
import com.hedera.hashgraph.hapi.model.SignatureMap;
import com.hedera.hashgraph.hapi.model.SignaturePair;
import com.hedera.hashgraph.hapi.model.TransactionBody;
import com.hedera.hashgraph.hapi.model.TransactionID;
import com.hedera.hashgraph.hapi.model.base.Duration;
import com.hedera.hashgraph.hapi.model.base.SignedTransaction;
import com.hedera.hashgraph.hapi.model.base.Timestamp;
import com.hedera.hashgraph.hapi.model.base.Transaction;
import com.hedera.hashgraph.hapi.model.token.CryptoCreateTransactionBody;
import com.hedera.hashgraph.hapi.writer.TransactionBodyWriter;
import com.hedera.hashgraph.hapi.writer.base.SignedTransactionWriter;
import com.hedera.hashgraph.hapi.writer.base.TransactionWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

class SimpleTest {
    private final BigInteger privateKeyData = new BigInteger(
            "302e020100300506032b65700422042091132178e72057a1d7528025956fe39b0b847f200ab59b2fdd367017f3087137", 16);

    @Test
    void test() throws IOException, InterruptedException {
        final var platform = new FakePlatform();
        final var hedera = new Hedera(platform);
        hedera.start();

        final var nodeAccount = Utils.accountID(2);
        final var payerAccount = Utils.accountID(1000);
        final var key = Utils.ecdsa(privateKeyData.toByteArray());
        final var txBodyData = new CryptoCreateTransactionBody.Builder()
                .key(key)
                .initialBalance(50_000_000_000L)
                .memo("Main Account")
                .build();

        final var txId = new TransactionID(now(), payerAccount, false, 0);
        final var txBody = new TransactionBody.Builder()
                .transactionID(txId)
                .nodeAccountID(nodeAccount)
                .transactionFee(10)
                .transactionValidDuration(new Duration(300))
                .cryptoCreateAccount(txBodyData)
                .build();

        var out = new ByteArrayOutputStream();
        TransactionBodyWriter.write(txBody, out);
        final var txBodyBytes = ByteBuffer.wrap(out.toByteArray());
        final var sig = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 });
        final var stx = new SignedTransaction(
                txBodyBytes,
                new SignatureMap(List.of(
                        new SignaturePair(
                                sig,
                                new OneOf<>(SignaturePair.SignatureOneOfType.ECDSA_SECP256K1, sig)))));

        out = new ByteArrayOutputStream();
        SignedTransactionWriter.write(stx, out);
        final var stxBytes = ByteBuffer.wrap(out.toByteArray());

        // This isn't right, I'm not actually signing anything, so sigs are all wrong, but whatever.
        final var tx = new Transaction(null, null, null, stxBytes, stxBytes);
        out = new ByteArrayOutputStream();
        TransactionWriter.write(tx, out);
        platform.createTransaction(out.toByteArray());

        while(true) {
            // Spin forever!!
            Thread.sleep(1);
        }
    }

    private Timestamp now() {
        return new Timestamp(Instant.now().getEpochSecond(), 0);
    }
}
