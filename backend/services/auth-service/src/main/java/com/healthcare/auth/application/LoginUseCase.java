package com.healthcare.auth.application;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}

