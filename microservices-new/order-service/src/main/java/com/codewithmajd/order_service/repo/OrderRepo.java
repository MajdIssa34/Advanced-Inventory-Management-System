package com.codewithmajd.order_service.repo;

import com.codewithmajd.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepo extends JpaRepository<Order,Long> {
    List<Order> findByTenantId(String tenantId);

    Optional<Order> findByOrderNumberAndTenantId(String orderNumber, String tenantId);

    @Query("SELECT o FROM Order o JOIN o.orderLineItemsList i WHERE i.skuCode = :skuCode AND o.tenantId = :tenantId")
    List<Order> findByLineItemSkuCodeAndTenantId(@Param("skuCode") String skuCode, @Param("tenantId") String tenantId);
}
