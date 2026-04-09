package com.healthcare.auth.application;

public interface LoginTokenIssuer {
    IssuedTokenPair issue(UserCredential userCredential);
}

