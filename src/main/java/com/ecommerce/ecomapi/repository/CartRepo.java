package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart , Long> {

    Optional<Cart> findByUserId(Long userId);
}
