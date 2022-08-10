package com.hedera.hashgraph.file.model;

import com.hedera.hashgraph.base.model.Timestamp;

public record FileInfo(
		FileID fileID,
		int size,
		Timestamp expirationTime,
		boolean deleted,
		/*KeyList keys, */
		String memo,
		byte[] ledgerID) {
}
