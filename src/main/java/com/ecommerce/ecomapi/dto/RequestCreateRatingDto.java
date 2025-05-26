package com.ecommerce.ecomapi.dto;

import lombok.Data;

@Data
public class RequestCreateRatingDto {
    private int stars;
    private String comment;
}
