package com.hedera.hashgraph.app;

import com.hedera.hashgraph.app.workflows.handle.HandleTransactionWorkflow;
import com.hedera.hashgraph.app.workflows.prehandle.PreHandleWorkflow;
import com.swirlds.common.Console;
import com.swirlds.common.InvalidSignedStateListener;
import com.swirlds.common.crypto.Cryptography;
import com.swirlds.common.crypto.SignatureType;
import com.swirlds.common.crypto.TransactionSignature;
import com.swirlds.common.io.streams.SerializableDataInputStream;
import com.swirlds.common.io.streams.SerializableDataOutputStream;
import com.swirlds.common.metrics.Metric;
import com.swirlds.common.statistics.Statistics;
import com.swirlds.common.system.NodeId;
import com.swirlds.common.system.Platform;
import com.swirlds.common.system.Round;
import com.swirlds.common.system.SwirldState;
import com.swirlds.common.system.address.Address;
import com.swirlds.common.system.events.ConsensusEvent;
import com.swirlds.common.system.events.Event;
import com.swirlds.common.system.events.PlatformEvent;
import com.swirlds.common.system.transaction.ConsensusTransaction;
import com.swirlds.common.system.transaction.SwirldTransaction;
import com.swirlds.common.system.transaction.Transaction;
import com.swirlds.common.system.transaction.TransactionType;
import com.swirlds.common.utility.AutoCloseableWrapper;

import javax.swing.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakePlatform implements Platform {
    private LinkedBlockingQueue<Transaction> ingestQueue = new LinkedBlockingQueue<>(1000);
    private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>(10);
    private AtomicLong roundNumber = new AtomicLong();

    private ExecutorService exe = Executors.newFixedThreadPool(2);
    private PreHandleWorkflow preHandleWorkflow;
    private HandleTransactionWorkflow handleTransactionWorkflow;

    public FakePlatform() {
    }

    public void start(PreHandleWorkflow preHandleWorkflow, HandleTransactionWorkflow handleTransactionWorkflow) {
        this.preHandleWorkflow = Objects.requireNonNull(preHandleWorkflow);
        this.handleTransactionWorkflow = Objects.requireNonNull(handleTransactionWorkflow);
        exe.submit(this::createEvent);
        exe.submit(this::createRound);
    }

    private void createEvent() {
        while (true) {
            try {
                final var transactions = new LinkedList<Transaction>();
                final var timeCreated = Instant.now();
                ingestQueue.drainTo(transactions);
                final var event = new FakeEvent(transactions, timeCreated);
                preHandleWorkflow.start(event);
                eventQueue.put(event);
            } catch (InterruptedException ex) {
                Thread.interrupted();
                ex.printStackTrace();
            }
        }
    }

    private void createRound() {
        while (true) {
            final var events = new LinkedList<Event>();
            eventQueue.drainTo(events);
            final var consensusOrder = new AtomicLong();
            final List<ConsensusEvent> consensusEvents = events.stream()
                    .map(e -> (ConsensusEvent) new FakeConsensusEvent(e, consensusOrder.getAndIncrement()))
                    .toList();

            final var round = roundNumber.getAndIncrement();
            final var r = new FakeRound(consensusEvents, round);
            handleTransactionWorkflow.start(r);
        }
    }

    @Override
    public void addAppStatEntry(Metric metric) {
        // This method is junk.
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAppMetrics(Metric... metrics) {

    }

    @Override
    public void addSignedStateListener(InvalidSignedStateListener invalidSignedStateListener) {

    }

    @Override
    public void appStatInit() {

    }

    @Override
    public Console createConsole(boolean b) {
        return null;
    }

    @Override
    public boolean createTransaction(byte[] bytes) {
        try {
            ingestQueue.put(new SwirldTransaction(bytes));
            return true;
        } catch (InterruptedException e) {
            Thread.interrupted();
            return false;
        }
    }

    @Override
    public JFrame createWindow(boolean b) {
        return null;
    }

    @Override
    public Instant estimateTime() {
        return null;
    }

    @Override
    public String getAbout() {
        return null;
    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public Address getAddress(long l) {
        return null;
    }

    @Override
    public PlatformEvent[] getAllEvents() {
        return new PlatformEvent[0];
    }

    @Override
    public long getLastGen(long l) {
        return 0;
    }

    @Override
    public int getNumMembers() {
        return 0;
    }

    @Override
    public String[] getParameters() {
        return new String[0];
    }

    @Override
    public NodeId getSelfId() {
        return null;
    }

    @Override
    public long getSleepAfterSync() {
        return 0;
    }

    @Override
    public <T extends SwirldState> T getState() {
        return null;
    }

    @Override
    public Statistics getStats() {
        return null;
    }

    @Override
    public byte[] getSwirldId() {
        return new byte[0];
    }

    @Override
    public boolean isMirrorNode() {
        return false;
    }

    @Override
    public boolean isZeroStakeNode() {
        return false;
    }

    @Override
    public void releaseState() {

    }

    @Override
    public void setAbout(String s) {

    }

    @Override
    public void setSleepAfterSync(long l) {

    }

    @Override
    public Cryptography getCryptography() {
        return null;
    }

    @Override
    public byte[] sign(byte[] bytes) {
        return new byte[0];
    }

    @Override
    public Instant getLastSignedStateTimestamp() {
        return null;
    }

    @Override
    public <T extends SwirldState> AutoCloseableWrapper<T> getLastCompleteSwirldState() {
        return null;
    }

    @Override
    public boolean isStateRecoveryInProgress() {
        return false;
    }

    private record FakeEvent(List<Transaction> transactions, Instant timeCreated) implements Event {

        @Override
        public Iterator<Transaction> transactionIterator() {
            return transactions.stream().iterator();
        }

        @Override
        public Instant getTimeCreated() {
            return timeCreated;
        }

        @Override
        public long getCreatorId() {
            return 0;
        }

        @Override
        public Instant getEstimatedTime() {
            return timeCreated;
        }
    }

    private record FakeConsensusEvent(Event other, long consensusOrder) implements ConsensusEvent {

        @Override
        public Iterator<ConsensusTransaction> consensusTransactionIterator() {
            final var itr = other.transactionIterator();
            return new Iterator<ConsensusTransaction>() {
                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public ConsensusTransaction next() {
                    return (ConsensusTransaction) itr.next();
                }
            };
        }

        @Override
        public long getConsensusOrder() {
            return consensusOrder;
        }

        @Override
        public Instant getConsensusTimestamp() {
            return other.getEstimatedTime();
        }

        @Override
        public Iterator<Transaction> transactionIterator() {
            return other.transactionIterator();
        }

        @Override
        public Instant getTimeCreated() {
            return other.getTimeCreated();
        }

        @Override
        public long getCreatorId() {
            return other.getCreatorId();
        }

        @Override
        public Instant getEstimatedTime() {
            return other.getEstimatedTime();
        }
    }

    private record FakeRound(List<ConsensusEvent> events, long round) implements Round {

        @Override
        public Iterator<ConsensusEvent> eventIterator() {
            return events.listIterator();
        }

        @Override
        public long getRoundNum() {
            return round;
        }
    }
}
