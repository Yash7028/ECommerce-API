package com.ecommerce.ecomapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDTO {
    private String id;
    private String userId;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;
}
