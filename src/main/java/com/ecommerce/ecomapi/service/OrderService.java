package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.dto.CartOrderRequestDto;
import com.ecommerce.ecomapi.dto.OrderCreateRequest;
import com.ecommerce.ecomapi.dto.PaymentIntentResponseDto;
import com.ecommerce.ecomapi.dto.PaymentRequest;
import com.ecommerce.ecomapi.entity.Address;
import com.ecommerce.ecomapi.entity.Order;
import com.ecommerce.ecomapi.entity.User;
import com.stripe.exception.StripeException;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    public Order createOrder(OrderCreateRequest request ,User user);
//    public Address processAddress(Address addressDTO, User user);
    public boolean deleteOrderById(Long orderId);
    public Page<Order> getAllOrders(int page, int size, String sortBy, String direction);
    public Order getOrderByIdWithDetails(Long orderId);
    public Page<Order> getOrdersByUserId(Long userId,int page, int size, String sortBy, String direction);
    public void deleteOrdersByUserId(Long userId);
    public Order createCartOrder(CartOrderRequestDto request , User user);
    public Page<Order> getOrdersBySellerId(String sellerId,int page, int size, String sortBy, String direction);
    public Page<Order> getOrdersBySellerIdItems(String sellerId, int page, int size, String sortBy, String direction);
    public Page<Order> getConfirmedOrdersBySeller(String sellerId, int page, int size, String sortBy, String direction);
    public boolean cancelOrder(Long oderId,String reasonMessage) throws StripeException;

    public PaymentIntentResponseDto doPayment(Long userId,PaymentRequest paymentRequest) throws StripeException;
}
