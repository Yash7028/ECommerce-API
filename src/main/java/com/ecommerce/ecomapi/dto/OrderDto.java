package com.ecommerce.ecomapi.dto;

import com.ecommerce.ecomapi.entity.Address;
import com.ecommerce.ecomapi.entity.Order;
import com.ecommerce.ecomapi.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private List<OrderItemDto> orderItems;
    private AddressDto billingAddress;
    private AddressDto shippingAddress;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime paymentDate;
    private Date deliveryDate;
    private LocalDateTime deliveredAt;
    private String cancelledReason;
    private String orderStatus;


    public OrderDto mapToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscount(order.getDiscount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentDate(order.getPaymentDate());
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setDeliveredAt(order.getDeliveredAt());
        dto.setCancelledReason(order.getCancelledReason());
        dto.setOrderStatus(order.getOrderStatus().toString());

        dto.setOrderItems(order.getOrderItems().stream()
                .map(this::mapToDto)
                .toList());

        dto.setBillingAddress(mapToDto(order.getBillingAddress()));
        dto.setShippingAddress(mapToDto(order.getShippingAddress()));

        return dto;
    }

    private OrderItemDto mapToDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setName(item.getName());
        dto.setQuantity(item.getQuantity());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setDiscountedPrice(item.getDiscountedPrice());
        dto.setMainImage(item.getMainImage());
        dto.setAdditionalImages(item.getAdditionalImages());
        return dto;
    }

    private AddressDto mapToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZip(address.getZip());
        dto.setCountry(address.getCountry());
        dto.setAddressType(address.getAddressType().toString());
        dto.setSaved(address.isSaved());
        return dto;
    }
}
