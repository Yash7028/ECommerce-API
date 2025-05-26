package com.ecommerce.ecomapi.dto;

import lombok.Data;

@Data
public class UpdateOrderStatusDto {
    public String orderStatus;
    public Long id;

}
