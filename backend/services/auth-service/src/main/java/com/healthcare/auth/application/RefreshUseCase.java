package com.healthcare.auth.application;

public interface RefreshUseCase {
    RefreshResult refresh(RefreshCommand command);
}

