package com.codewithmajd.repository;

import com.codewithmajd.developer_service.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    List<ApiKey> findByTenantIdAndIsRevokedFalse(String tenantId);

    Optional<ApiKey> findByIdAndTenantId(Long id, String tenantId);
}