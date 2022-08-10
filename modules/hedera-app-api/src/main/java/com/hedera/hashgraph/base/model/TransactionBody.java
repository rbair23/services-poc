package com.hedera.hashgraph.base.model;

import com.hedera.hashgraph.protoparse.OneOf;

// NOTE: Another way to handle "oneof", which we need to get modularity right, where we just treat it as a byte[].
// At some point you can deserialize it lazily and stash the type in there. But we don't need to do it now
// (and maybe never!)
public record TransactionBody(
		TransactionID transactionID,
		AccountID accountID,
		long transactionFee,
		Duration transactionValidDuration,
		String memo,
		OneOf<?> data) {
}
