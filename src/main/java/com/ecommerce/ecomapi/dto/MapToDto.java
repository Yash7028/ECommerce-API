package com.ecommerce.ecomapi.dto;

import com.ecommerce.ecomapi.entity.Address;
import com.ecommerce.ecomapi.entity.Order;
import com.ecommerce.ecomapi.entity.OrderItem;

import java.util.stream.Collectors;

public class MapToDto {

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

        if (order.getOrderStatus() != null) {
            dto.setOrderStatus(order.getOrderStatus().toString());
        }

        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
        }

        if (order.getBillingAddress() != null) {
            dto.setBillingAddress(mapToDto(order.getBillingAddress()));
        }

        if (order.getShippingAddress() != null) {
            dto.setShippingAddress(mapToDto(order.getShippingAddress()));
        }

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

        // Add null check here
        if (address.getAddressType() != null) {
            dto.setAddressType(address.getAddressType().toString());
        } else {
            dto.setAddressType(null); // Or set to a default value like "UNKNOWN"
        }

        dto.setSaved(address.isSaved());
        return dto;
    }

}
