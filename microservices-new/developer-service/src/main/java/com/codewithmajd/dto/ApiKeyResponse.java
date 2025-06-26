package com.codewithmajd.dto;

import java.time.LocalDateTime;

public record ApiKeyResponse(
        Long id,
        String name,
        String keyPrefix,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt
) {
}
