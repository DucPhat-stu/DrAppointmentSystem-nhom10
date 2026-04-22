package com.healthcare.auth.service.login;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
