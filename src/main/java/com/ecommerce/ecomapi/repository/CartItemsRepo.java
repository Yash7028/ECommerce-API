package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemsRepo extends JpaRepository<CartItem , Long> {
   Optional<CartItem> findByCartIdAndProductId(Long cartId, String productId);
   List<CartItem> findByCartId(Long id);
}
