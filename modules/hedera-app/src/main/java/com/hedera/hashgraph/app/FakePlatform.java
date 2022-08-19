package com.hedera.hashgraph.app;

import com.swirlds.common.Console;
import com.swirlds.common.InvalidSignedStateListener;
import com.swirlds.common.crypto.Cryptography;
import com.swirlds.common.metrics.Metric;
import com.swirlds.common.statistics.Statistics;
import com.swirlds.common.system.NodeId;
import com.swirlds.common.system.Platform;
import com.swirlds.common.system.SwirldState;
import com.swirlds.common.system.address.Address;
import com.swirlds.common.system.events.PlatformEvent;
import com.swirlds.common.utility.AutoCloseableWrapper;

import javax.swing.*;
import java.time.Instant;

class FakePlatform implements Platform {
    @Override
    public void addAppStatEntry(Metric metric) {
        // This method is junk.
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
        return false;
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
}
