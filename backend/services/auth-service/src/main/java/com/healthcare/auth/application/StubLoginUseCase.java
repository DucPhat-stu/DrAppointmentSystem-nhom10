package com.healthcare.auth.application;

import com.healthcare.shared.api.ErrorCode;
import com.healthcare.shared.common.exception.ApiException;
import org.springframework.stereotype.Service;

@Service
public class StubLoginUseCase implements LoginUseCase {
    @Override
    public LoginResult login(LoginCommand command) {
        throw new ApiException(
                ErrorCode.SERVICE_UNAVAILABLE,
                "Login credential verification and token issuing are not implemented yet"
        );
    }
}

