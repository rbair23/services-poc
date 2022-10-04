package com.hedera.hashgraph.token;

import com.hedera.hashgraph.base.QueryHandler;
import com.hedera.hashgraph.hapi.model.token.TokenGetInfoQuery;
import com.hedera.hashgraph.hapi.model.token.TokenGetNftInfoQuery;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A {@link QueryHandler} for responding to queries defined in the protobuf "TokenService".
 */
public interface TokenQueryHandler extends QueryHandler {
    /**
     * Retrieves the metadata of a token
     */
    void getTokenInfo(@NonNull TokenGetInfoQuery query);

    /**
     * Retrieves the metadata of an NFT by TokenID and serial number
     */
    void getTokenNftInfo(@NonNull TokenGetNftInfoQuery query);
}
