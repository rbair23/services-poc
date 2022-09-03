package com.hedera.hashgraph.hapi;

import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody.MemoFieldOneOfType;
import com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody.StakedIdOneOfType;
import com.hedera.hashgraph.hapi.parser.TokenTransferListProtoParser;
import com.hedera.hashgraph.hapi.parser.contract.ContractLoginfoProtoParser;
import com.hedera.hashgraph.hapi.parser.contract.ContractUpdateTransactionBodyProtoParser;
import com.hedera.hashgraph.hapi.writer.TokenTransferListWriter;
import com.hedera.hashgraph.hapi.writer.contract.ContractLoginfoWriter;
import com.hedera.hashgraph.hapi.writer.contract.ContractUpdateTransactionBodyWriter;
import com.hedera.hashgraph.protoparse.MalformedProtobufException;
import com.hederahashgraph.api.proto.java.ContractLoginfo;
import com.hederahashgraph.api.proto.java.ContractUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.TokenID;
import com.hederahashgraph.api.proto.java.TokenTransferList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * There are many special edge cases in protobuf. This is some handwritten tests to help debug and fix them.
 */
public class EdgeCaseTests {

    @ParameterizedTest()
    @MethodSource("createUints")
    public void testIntValue(UInt32Value value) throws IOException, MalformedProtobufException {
        System.out.println("\n\n====== value = " + value);
        if (value != null) System.out.println("====== value.getValue() = " + value.getValue());
        final var ttlBuilder = TokenTransferList.newBuilder()
                .setToken(TokenID.newBuilder()
                        .setShardNum(1)
                        .setRealmNum(2)
                        .setTokenNum(3)
                        .build());
        if (value != null) {
            ttlBuilder.setExpectedDecimals(value);
        } else {
            ttlBuilder.clearExpectedDecimals();
        }
        TokenTransferList ttl = ttlBuilder.build();
        final byte[] bytes = ttl.toByteArray();
        printProtoBuf("bytes",bytes);

        com.hedera.hashgraph.hapi.model.TokenTransferList ttlRecord = new
                com.hedera.hashgraph.hapi.model.TokenTransferList(
                        new com.hedera.hashgraph.hapi.model.TokenID(1,2,3),
                Collections.emptyList(),
                Collections.emptyList(),
                value == null ? Optional.empty() : Optional.of(value.getValue())
                );
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        TokenTransferListWriter.write(ttlRecord,bout);
        bout.flush();
        bout.close();
        byte[] recordBytes = bout.toByteArray();
        printProtoBuf("recordBytes",recordBytes);
        assertEquals(bytes.length, recordBytes.length);
        assertArrayEquals(bytes,recordBytes);

        TokenTransferList ttl2 = TokenTransferList.parseFrom(bytes);
        System.out.println("ttl.getExpectedDecimals() = " + ttl.getExpectedDecimals());
        System.out.println("ttl.hasExpectedDecimals() = " + ttl.hasExpectedDecimals());
        System.out.println("ttl.getExpectedDecimals() == null = " + ttl.getExpectedDecimals() == null);
        System.out.println("ttl.getExpectedDecimals().getClass() = " + ttl.getExpectedDecimals().getClass());
        System.out.println("ttl.getExpectedDecimals().getValue() = " + ttl.getExpectedDecimals().getValue());
        System.out.println("ttl2.getExpectedDecimals() = " + ttl2.getExpectedDecimals());
        assertEquals(ttl.getExpectedDecimals(), ttl2.getExpectedDecimals());
        assertEquals(ttl, ttl2);

        var ttl3 = new TokenTransferListProtoParser().parse(bytes);
        assertEquals(ttlRecord, ttl3);
    }

    @Test
    public void testEmptyBytesArrays() throws MalformedProtobufException, IOException {
        final ContractLoginfo contractLoginfo1 = ContractLoginfo.newBuilder()
                .addTopic(ByteString.EMPTY)
                .build();
        System.out.println("contractLoginfo1.getTopicCount() = " + contractLoginfo1.getTopicCount());
        final byte[] bytes1 = contractLoginfo1.toByteArray();
        printProtoBuf("bytes1",bytes1);
        final ContractLoginfo contractLoginfo2 = ContractLoginfo.newBuilder()
                .build();
        System.out.println("contractLoginfo2.getTopicCount() = " + contractLoginfo2.getTopicCount());
        final byte[] bytes2 = contractLoginfo2.toByteArray();
        printProtoBuf("bytes2",bytes2);
        assertFalse(Arrays.equals(bytes1,bytes2));

        var contractLoginfo3 = new ContractLoginfoProtoParser().parse(bytes1);
        System.out.println("contractLoginfo3 = " + contractLoginfo3);
        var contractLoginfo4 = new com.hedera.hashgraph.hapi.model.contract.ContractLoginfo(
                null,
                ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer(),
                List.of(ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer()),
                ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer()
        );
        System.out.println("contractLoginfo4 = " + contractLoginfo4);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ContractLoginfoWriter.write(contractLoginfo4, bout);
        final byte[] bytes4 = bout.toByteArray();
        printProtoBuf("bytes4",bytes4);
        assertArrayEquals(bytes1, bytes4);
    }

    /**
     * Make sure we can write and parse protobufs with oneOfs that contain the default values.
     */
    @Test
    public void testOneOfsWithDefaultValues() throws MalformedProtobufException, IOException {
        final ContractUpdateTransactionBody protoEmpty = ContractUpdateTransactionBody.newBuilder()
                .build();
        final byte[] protoEmptyBytes = protoEmpty.toByteArray();
        printProtoBuf("protoEmptyBytes",protoEmptyBytes);
        final ContractUpdateTransactionBody protoWithMemo = ContractUpdateTransactionBody.newBuilder()
                .setMemo("")
                .build();
        final byte[] protoWithMemoBytes = protoWithMemo.toByteArray();
        printProtoBuf("protoWithMemoBytes",protoWithMemoBytes);
        final ContractUpdateTransactionBody protoWithMemoWrapper = ContractUpdateTransactionBody.newBuilder()
                .setMemoWrapper(StringValue.of(""))
                .build();
        final byte[] protoWithMemoWrapperBytes = protoWithMemoWrapper.toByteArray();
        printProtoBuf("protoWithMemoWrapperBytes",protoWithMemoWrapperBytes);

        final com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody modelEmpty =
                new com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new OneOf<>(MemoFieldOneOfType.UNSET,null),
                        Optional.empty(),
                        null,
                        new OneOf<>(StakedIdOneOfType.UNSET, null),
                        Optional.empty()
                );
        final ByteArrayOutputStream bout3 = new ByteArrayOutputStream();
        ContractUpdateTransactionBodyWriter.write(modelEmpty,bout3);
        final byte[] modelEmptyBytes = bout3.toByteArray();
        printProtoBuf("modelEmptyBytes",modelEmptyBytes);

        final com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody modelWithMemo =
                new com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new OneOf<>(MemoFieldOneOfType.MEMO,""),
                        Optional.empty(),
                        null,
                        new OneOf<>(StakedIdOneOfType.UNSET, null),
                        Optional.empty()
                );
        final ByteArrayOutputStream bout4 = new ByteArrayOutputStream();
        ContractUpdateTransactionBodyWriter.write(modelWithMemo,bout4);
        final byte[] modelWithMemoBytes = bout4.toByteArray();
        printProtoBuf("modelWithMemoBytes",modelWithMemoBytes);

        final com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody modelWithMemoWrapper =
                new com.hedera.hashgraph.hapi.model.contract.ContractUpdateTransactionBody(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new OneOf<>(MemoFieldOneOfType.MEMO_WRAPPER,Optional.of("")),
                        Optional.empty(),
                        null,
                        new OneOf<>(StakedIdOneOfType.UNSET, null),
                        Optional.empty()
                );
        final ByteArrayOutputStream bout5 = new ByteArrayOutputStream();
        ContractUpdateTransactionBodyWriter.write(modelWithMemoWrapper,bout5);
        final byte[] modelWithMemoWrapperBytes = bout5.toByteArray();
        printProtoBuf("modelWithMemoWrapperBytes",modelWithMemoWrapperBytes);

        assertArrayEquals(protoEmptyBytes, modelEmptyBytes);
        assertArrayEquals(protoWithMemoBytes, modelWithMemoBytes);
        assertArrayEquals(protoWithMemoWrapperBytes, modelWithMemoWrapperBytes);

        final var readEmpty = new ContractUpdateTransactionBodyProtoParser().parse(modelEmptyBytes);
        final var readWithMemo = new ContractUpdateTransactionBodyProtoParser().parse(modelWithMemoBytes);
        final var readWithMemoWrapper = new ContractUpdateTransactionBodyProtoParser().parse(modelWithMemoWrapperBytes);
        assertEquals(modelEmpty, readEmpty);
        assertEquals(modelWithMemo, readWithMemo);
        assertEquals(modelWithMemoWrapper, readWithMemoWrapper);
    }

    public static final Stream<UInt32Value> createUints() {
        return Stream.of(
            null,
            UInt32Value.newBuilder().setValue(0).build(),
            UInt32Value.newBuilder().setValue(1).build(),
            UInt32Value.newBuilder().setValue(100).build()
        );
    }

    public static final void printProtoBuf(String name, byte[] bytes) {
        System.out.printf("%15s",name);
        for (int i = 0; i < bytes.length; i++) {
            System.out.printf("%11d,",bytes[i]);
        }
        System.out.println("");
        System.out.printf("%15s","[field|type]");
        for (int i = 0; i < bytes.length; i++) {
            // The field is the top 5 bits of the byte. Read this off
            final int field = bytes[i] >> TAG_FIELD_OFFSET;
            // The wire type is the bottom 3 bits of the byte. Read that off
            final int wireType = bytes[i] & TAG_WRITE_TYPE_MASK;
            System.out.printf("[%4d|%4d],",field,wireType);
        }
        System.out.println("");
    }


    /**
     * The number of lower order bits from the "tag" byte that should be rotated out
     * to reveal the field number
     */
    static final int TAG_FIELD_OFFSET = 3;
    /**
     * Mask used to extract the wire type from the "tag" byte
     */
    static final int TAG_WRITE_TYPE_MASK = 0b0000_0111;
}
/*

ContractUpdateTransactionBody[
    contractID=ContractID[
        shardNum=0, realmNum=0, contract=OneOf[
            kind=CONTRACT_NUM, value=-21]], expirationTime=Timestamp[
            seconds=0, nanos=0], adminKey=Key[
                key=OneOf[
                kind=CONTRACT_ID,
                value=ContractID[
                    shardNum=-21,
                    realmNum=-21,
                    contract=OneOf[
                        kind=CONTRACT_NUM,
                        value=-42
                    ]
                ]
            ]
        ],
        proxyAccountID=AccountID[
            shardNum=0,
            realmNum=0,
            account=OneOf[
                kind=ACCOUNT_NUM,
                value=-21]
            ],
            autoRenewPeriod=Duration[seconds=0],
            fileID=FileID[shardNum=0, realmNum=0, fileNum=0],
            memoField=OneOf[kind=MEMO_WRAPPER, value=Optional.empty],
            maxAutomaticTokenAssociations=Optional[-21],
            autoRenewAccountId=AccountID[
                shardNum=0,
                realmNum=0,
                account=OneOf[
                    kind=ACCOUNT_NUM, value=-21]
                ],
                stakedId=OneOf[
                    kind=STAKED_ACCOUNT_ID,
                    value=AccountID[
                        shardNum=-21,
                        realmNum=-21,
                        account=OneOf[
                            kind=ACCOUNT_NUM, value=-42
                        ]
                    ]
                ],
                declineReward=Optional[
                    false
                ]
            ]
ContractUpdateTransactionBody[contractID=ContractID[shardNum=0, realmNum=0, contract=OneOf[kind=CONTRACT_NUM, value=-21]], expirationTime=Timestamp[seconds=0, nanos=0], adminKey=Key[key=OneOf[kind=CONTRACT_ID, value=ContractID[shardNum=-21, realmNum=-21, contract=OneOf[kind=CONTRACT_NUM, value=-42]]]], proxyAccountID=AccountID[shardNum=0, realmNum=0, account=OneOf[kind=ACCOUNT_NUM, value=-21]], autoRenewPeriod=Duration[seconds=0], fileID=FileID[shardNum=0, realmNum=0, fileNum=0], memoField=OneOf[kind=UNSET, value=null], maxAutomaticTokenAssociations=Optional[-21], autoRenewAccountId=AccountID[shardNum=0, realmNum=0, account=OneOf[kind=ACCOUNT_NUM, value=-21]], stakedId=OneOf[kind=STAKED_ACCOUNT_ID, value=AccountID[shardNum=-21, realmNum=-21, account=OneOf[kind=ACCOUNT_NUM, value=-42]]], declineReward=Optional[false]]

 */