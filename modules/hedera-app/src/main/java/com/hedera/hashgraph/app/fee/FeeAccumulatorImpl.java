package com.hedera.hashgraph.app.fee;

import com.hedera.hashgraph.base.FeeAccumulator;
import com.hedera.hashgraph.hapi.model.FeeData;
import com.hedera.hashgraph.hapi.model.HederaFunctionality;

import java.util.Objects;

/**
 * An implementation of {@link FeeAccumulator}.
 */
public final class FeeAccumulatorImpl implements FeeAccumulator {
	private final FeeScheduleLookup lookup;

	private long node_min;
	private long node_max;
	private long node_constant;
	private long node_bpt;
	private long node_vpt;
	private long node_rbh;
	private long node_sbh;
	private long node_gas;
	private long node_tv;
	private long node_bpr;
	private long node_sbpr;

	private long network_min;
	private long network_max;
	private long network_constant;
	private long network_bpt;
	private long network_vpt;
	private long network_rbh;
	private long network_sbh;
	private long network_gas;
	private long network_tv;
	private long network_bpr;
	private long network_sbpr;

	private long service_min;
	private long service_max;
	private long service_constant;
	private long service_bpt;
	private long service_vpt;
	private long service_rbh;
	private long service_sbh;
	private long service_gas;
	private long service_tv;
	private long service_bpr;
	private long service_sbpr;

	public FeeAccumulatorImpl(FeeScheduleLookup lookup) {
		this.lookup = Objects.requireNonNull(lookup);
	}

	@Override
	public void accumulate(HederaFunctionality feature) {
		accumulate(this.lookup.feeData(feature));
	}

	private void accumulate(FeeData feeData) {
		node_min += feeData.nodedata().min();
		node_max += feeData.nodedata().max();
		node_constant += feeData.nodedata().constant();
		node_bpt += feeData.nodedata().bpt();
		node_vpt += feeData.nodedata().vpt();
		node_rbh += feeData.nodedata().rbh();
		node_sbh += feeData.nodedata().sbh();
		node_gas += feeData.nodedata().gas();
		node_tv += feeData.nodedata().tv();
		node_bpr += feeData.nodedata().bpr();
		node_sbpr += feeData.nodedata().sbpr();

		network_min += feeData.networkdata().min();
		network_max += feeData.networkdata().max();
		network_constant += feeData.networkdata().constant();
		network_bpt += feeData.networkdata().bpt();
		network_vpt += feeData.networkdata().vpt();
		network_rbh += feeData.networkdata().rbh();
		network_sbh += feeData.networkdata().sbh();
		network_gas += feeData.networkdata().gas();
		network_tv += feeData.networkdata().tv();
		network_bpr += feeData.networkdata().bpr();
		network_sbpr += feeData.networkdata().sbpr();

		service_min += feeData.servicedata().min();
		service_max += feeData.servicedata().max();
		service_constant += feeData.servicedata().constant();
		service_bpt += feeData.servicedata().bpt();
		service_vpt += feeData.servicedata().vpt();
		service_rbh += feeData.servicedata().rbh();
		service_sbh += feeData.servicedata().sbh();
		service_gas += feeData.servicedata().gas();
		service_tv += feeData.servicedata().tv();
		service_bpr += feeData.servicedata().bpr();
		service_sbpr += feeData.servicedata().sbpr();
	}
}
