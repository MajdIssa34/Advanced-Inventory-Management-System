package com.codewithmajd.controller;

import com.codewithmajd.dto.ApiKeyResponse;
import com.codewithmajd.dto.CreateApiKeyRequest;
import com.codewithmajd.dto.NewApiKeyResponse;
import com.codewithmajd.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/developer")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping("/keys")
    public ResponseEntity<List<ApiKeyResponse>> getApiKeysForTenant(@RequestHeader("X-Tenant-ID") String tenantId) {
        List<ApiKeyResponse> keys = apiKeyService.getApiKeysForTenant(tenantId);
        return ResponseEntity.ok(keys);
    }

    @PostMapping("/keys")
    public ResponseEntity<NewApiKeyResponse> createApiKey(@RequestHeader("X-Tenant-ID") String tenantId,
                                                          @Valid @RequestBody CreateApiKeyRequest request) {
        NewApiKeyResponse newApiKey = apiKeyService.createApiKey(request.name(), tenantId);
        return new ResponseEntity<>(newApiKey, HttpStatus.CREATED);
    }

    @DeleteMapping("/keys/{keyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeApiKey(@RequestHeader("X-Tenant-ID") String tenantId,
                             @PathVariable Long keyId) {
        apiKeyService.revokeApiKey(keyId, tenantId);
    }
}