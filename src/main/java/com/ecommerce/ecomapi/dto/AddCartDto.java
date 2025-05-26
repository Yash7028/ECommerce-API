package com.ecommerce.ecomapi.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class AddCartDto{
    private String productId;
    private int quantity;
}
