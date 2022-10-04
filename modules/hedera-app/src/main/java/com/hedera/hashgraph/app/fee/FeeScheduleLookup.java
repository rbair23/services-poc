package com.hedera.hashgraph.app.fee;

import com.hedera.hashgraph.hapi.model.FeeData;
import com.hedera.hashgraph.hapi.model.FeeSchedule;
import com.hedera.hashgraph.hapi.model.HederaFunctionality;

import java.util.HashMap;
import java.util.Map;

public final class FeeScheduleLookup {
    private final Map<HederaFunctionality, FeeData> feeData = new HashMap<>();

    public FeeScheduleLookup(FeeSchedule schedule) {
        schedule.transactionFeeSchedule().forEach(sched -> {
            // Are we supposed to deal with lists of fee schedules?
            feeData.put(sched.hederaFunctionality(), sched.feeData());
        });
    }

    public FeeData feeData(HederaFunctionality feature) {
        return feeData.get(feature);
    }
}
