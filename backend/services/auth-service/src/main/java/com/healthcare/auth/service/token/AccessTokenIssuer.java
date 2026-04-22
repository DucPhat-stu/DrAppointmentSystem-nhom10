package com.healthcare.auth.service.token;

import com.healthcare.auth.service.login.UserCredential;

public interface AccessTokenIssuer {
    AccessTokenResult issueAccessToken(UserCredential userCredential);
}
