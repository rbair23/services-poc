package com.hedera.hashgraph.app;

import com.hedera.hashgraph.file.FileService;
import com.hedera.hashgraph.token.AccountService;
import com.hedera.hashgraph.token.TokenService;

public record ServicesAccessor(
        AccountService accountService,
        FileService fileService,
        TokenService tokenService) {
}
