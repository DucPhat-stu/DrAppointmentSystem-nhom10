package com.healthcare.shared.common.web;

import com.healthcare.shared.api.ApiMeta;

public interface RequestMetadataProvider {
    ApiMeta current();
}

