package com.healthcare.auth.service.token;

public interface RefreshUseCase {
    RefreshResult refresh(RefreshCommand command);
}
