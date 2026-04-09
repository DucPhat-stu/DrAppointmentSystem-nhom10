package com.healthcare.shared.common.web;

public interface RequestMetadataContext extends RequestMetadataProvider {
    void open(String requestId);

    void clear();
}

