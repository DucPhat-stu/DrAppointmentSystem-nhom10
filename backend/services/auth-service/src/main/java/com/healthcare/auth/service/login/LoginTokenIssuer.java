package com.healthcare.auth.service.login;

public interface LoginTokenIssuer {
    IssuedTokenPair issue(UserCredential userCredential);
}
