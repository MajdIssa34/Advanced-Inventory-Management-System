package com.codewithmajd.dto;

import java.time.LocalDateTime;

public record NewApiKeyResponse(
        Long id,
        String name,
        String keyPrefix,
        LocalDateTime createdAt,
        String plainTextKey
) {}
