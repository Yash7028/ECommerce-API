package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.dto.CartDto;
import com.ecommerce.ecomapi.entity.Cart;
import org.bson.types.ObjectId;

public interface CartService {
    public Cart addProuctToCart(Long userId, ObjectId productId, Integer quantity);
    public void removeCartItem(Long cartItemId);
    public CartDto getCartByUserId(Long userId);
    public void updateCartItemQuantity(Long itemId, int quantity);
    public void  clearCart(Long id);
}
