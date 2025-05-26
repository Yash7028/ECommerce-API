package com.ecommerce.ecomapi.dto;

import com.ecommerce.ecomapi.validator.ValidProductUpdate;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidProductUpdate
public class ProductUpdateDTO {

    private String productName;

    private String description;

    private BigDecimal price;

    private Integer quantity;

    private String brand;

    private String sku;

    private String category;

    private String subCategory;

    private List<String> tags;

    private BigDecimal discountedPrice;

    private Boolean isFeatured;
}
