package com.codewithmajd.service;

import com.codewithmajd.dto.ApiKeyResponse;
import com.codewithmajd.dto.NewApiKeyResponse;
import com.codewithmajd.developer_service.model.ApiKey;
import com.codewithmajd.repository.ApiKeyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String KEY_PREFIX = "sk_prod_";

    public NewApiKeyResponse createApiKey(String name, String tenantId) {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
        String plainTextKey = KEY_PREFIX + randomPart;
        String keyHash = passwordEncoder.encode(plainTextKey);

        ApiKey apiKey = ApiKey.builder()
                .name(name)
                .tenantId(tenantId)
                .keyPrefix(KEY_PREFIX)
                .keyHash(keyHash)
                .isRevoked(false)
                .build();

        ApiKey savedApiKey = apiKeyRepository.save(apiKey);
        log.info("Created new API key with ID {} for tenant {}", savedApiKey.getId(), tenantId);

        return new NewApiKeyResponse(
                savedApiKey.getId(),
                savedApiKey.getName(),
                savedApiKey.getKeyPrefix(),
                savedApiKey.getCreatedAt(),
                plainTextKey
        );
    }

    public List<ApiKeyResponse> getApiKeysForTenant(String tenantId) {
        return apiKeyRepository.findByTenantIdAndIsRevokedFalse(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void revokeApiKey(Long keyId, String tenantId) {
        ApiKey apiKey = apiKeyRepository.findByIdAndTenantId(keyId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("API Key not found or you do not have permission to revoke it."));

        apiKey.setRevoked(true);
        apiKeyRepository.save(apiKey);
        log.info("Revoked API key with ID {} for tenant {}", keyId, tenantId);
    }

    private ApiKeyResponse mapToResponse(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                apiKey.getCreatedAt(),
                apiKey.getLastUsedAt()
        );
    }
}