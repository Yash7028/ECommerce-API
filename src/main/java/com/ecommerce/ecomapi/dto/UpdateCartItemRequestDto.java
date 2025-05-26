package com.ecommerce.ecomapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartItemRequestDto {
    private Long itemId;
    private int quantity;
}
