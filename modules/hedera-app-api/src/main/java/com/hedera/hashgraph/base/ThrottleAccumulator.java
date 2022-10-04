package com.hedera.hashgraph.base;

import com.hedera.hashgraph.hapi.model.HederaFunctionality;

/**
 * Keeps track of the amount of usage of different throttle categories (by {@code id}),
 * and returns whether the throttle has been exceeded after applying the given incremental
 * amount.
 */
public interface ThrottleAccumulator {
	/**
	 * Increments the throttle associated with {@code id} and returns whether the throttle
	 * has been exceeded. If there is no throttle associated with {@code id}, then an
	 * {@link IllegalArgumentException} will be thrown. This is to prevent bugs where
	 * some code accidentally specified a throttle but a corresponding throttle was never
	 * configured, leading to an open-throttle situation (i.e. an un-throttled attack vector).
	 *
	 * @param id The ID of the throttle to increment and check. This must exist.
	 * @param incrementalAmount The amount to increment by before checking the throttle. This
	 *                          may be 0 if you simply want to read whether the throttle is
	 *                          exceeded without incrementing it. A negative value is not
	 *                          permitted.
	 * @return true if the throttle has been exceeded, false otherwise.
	 */
	boolean shouldThrottle(HederaFunctionality id, int incrementalAmount);
	// NOTE: Be sure to test the actual implementation against insane incremental amounts like MAX_VALUE that can cause overflow
}
