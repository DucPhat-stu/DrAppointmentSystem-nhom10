package com.healthcare.auth.application;

public interface AccessTokenIssuer {
    AccessTokenResult issueAccessToken(UserCredential userCredential);
}

