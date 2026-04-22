package com.healthcare.auth.service.token;

public interface LogoutUseCase {
    void logout(LogoutCommand command);
}
