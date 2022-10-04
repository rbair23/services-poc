package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.Service;
import com.hedera.hashgraph.base.state.States;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The {@code TokenQueryHandler} is responsible for working with {@code Token}s. It implements
 * all transactions nad queries defined in the "TokenService" protobuf.
 */
public interface TokenService extends Service {
	/**
	 * Creates and returns a new {@link TokenTransactionHandler}
	 *
	 * @return A new {@link TokenTransactionHandler}
	 */
	@Override
	@NonNull TokenTransactionHandler createTransactionHandler(@NonNull States states);

	/**
	 * Creates and returns a new {@link TokenQueryHandler}
	 *
	 * @return A new {@link TokenQueryHandler}
	 */
	@Override
	@NonNull TokenQueryHandler createQueryHandler(@NonNull States states);
}
