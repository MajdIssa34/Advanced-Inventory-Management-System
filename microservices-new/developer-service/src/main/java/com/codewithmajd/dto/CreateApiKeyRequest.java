package com.codewithmajd.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record CreateApiKeyRequest(
        @NotBlank(message = "Key name cannot be blank")
        String name
) {}

