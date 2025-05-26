package com.ecommerce.ecomapi.dto;

import com.ecommerce.ecomapi.entity.Rating;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRatingResponseDto {
    private double averageRating;
    private List<Rating> ratings;
}
