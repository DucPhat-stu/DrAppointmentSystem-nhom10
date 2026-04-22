package com.healthcare.auth.service.token;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface RefreshTokenLifecycleManager {
    void markUsed(UUID tokenId, OffsetDateTime usedAt);

    void revoke(UUID tokenId, OffsetDateTime revokedAt);
}
