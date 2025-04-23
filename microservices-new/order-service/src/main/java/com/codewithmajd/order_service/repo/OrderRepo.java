package com.codewithmajd.order_service.repo;

import com.codewithmajd.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order,Long> {


}
