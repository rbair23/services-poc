package com.hedera.hashgraph.app;

import com.hedera.hashgraph.file.FileService;
import com.hedera.hashgraph.token.CryptoService;
import com.hedera.hashgraph.token.TokenService;

public record ServicesAccessor(
        ServicesContext<CryptoService> cryptoService,
        ServicesContext<FileService> fileService,
        ServicesContext<TokenService> tokenService) {
}
