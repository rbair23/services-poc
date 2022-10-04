package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.Service;
import com.hedera.hashgraph.base.state.States;
import com.hedera.hashgraph.token.entity.Account;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The {@code CryptoService} is responsible for working with {@link Account}s. It implements
 * all transactions nad queries defined in the "CryptoService" protobuf
 * service. The {@code CryptoService} is used extensively by the core application workflows
 * to implement transaction handling, since all transactions and most queries involve payments
 * and thus the transfer of HBAR from one account to another. A {@link CryptoTransactionHandler}
 * contains API for all transactions related to crypto (and token) transfers, as well as some
 * additional API needed by the core application to apply payments and compute rewards.
 */
public interface CryptoService extends Service {
	/**
	 * Creates and returns a new {@link CryptoTransactionHandler}
	 *
	 * @return A new {@link CryptoTransactionHandler}
	 */
	@Override
	@NonNull CryptoTransactionHandler createTransactionHandler(@NonNull States states);

	/**
	 * Creates and returns a new {@link CryptoQueryHandler}
	 *
	 * @return A new {@link CryptoQueryHandler}
	 */
	@Override
	@NonNull CryptoQueryHandler createQueryHandler(@NonNull States states);
}
