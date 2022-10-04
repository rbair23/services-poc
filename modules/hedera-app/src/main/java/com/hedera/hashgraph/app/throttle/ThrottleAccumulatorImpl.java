package com.hedera.hashgraph.app.throttle;

import com.hedera.hashgraph.base.ThrottleAccumulator;
import com.hedera.hashgraph.hapi.model.HederaFunctionality;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

// NOTE: This is a demo implementation, not production worthy. A real implementation would rely on the
// throttle engine and would check for overflow when applying incremental amount and would check for
// missing throttles and so forth. This is just a quick and very dirty and very wrong implementation for
// POC purposes.
@ThreadSafe
public class ThrottleAccumulatorImpl implements ThrottleAccumulator {
	private final Map<HederaFunctionality, AtomicLong> throttles = new HashMap<>();

	@Override
	public boolean shouldThrottle(final HederaFunctionality id, final int incrementalAmount) {
		var throttle = throttles.get(id);
		// Please note, this violates the spec of this method, but is OK for quick and dirty POC
		if (throttle == null) {
			throttle = new AtomicLong(0);
			throttles.put(id, throttle);
		}

		// This is also wrong, it just assumes we throttle at 100 and then resets, and doesn't
		// take time into account. A real implementation of this class would do better.
		var val = throttle.addAndGet(incrementalAmount);
		if (val > 100) {
			throttle.set(0);
			return true;
		}

		return false;
	}
}
