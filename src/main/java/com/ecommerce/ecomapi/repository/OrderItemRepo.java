package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProductIdIn(List<String> productIds);
}
