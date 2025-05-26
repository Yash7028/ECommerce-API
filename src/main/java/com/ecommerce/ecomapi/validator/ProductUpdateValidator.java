package com.ecommerce.ecomapi.validator;

import com.ecommerce.ecomapi.dto.ProductUpdateDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class ProductUpdateValidator implements ConstraintValidator<ValidProductUpdate, ProductUpdateDTO> {

    @Override
    public boolean isValid(ProductUpdateDTO dto, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (dto.getProductName() != null && dto.getProductName().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Product name cannot be blank")
                    .addPropertyNode("productName").addConstraintViolation();
            isValid = false;
        }

        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            context.buildConstraintViolationWithTemplate("Description must not exceed 1000 characters")
                    .addPropertyNode("description").addConstraintViolation();
            isValid = false;
        }

        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            context.buildConstraintViolationWithTemplate("Price must be greater than zero")
                    .addPropertyNode("price").addConstraintViolation();
            isValid = false;
        }

        if (dto.getQuantity() != null && dto.getQuantity() < 0) {
            context.buildConstraintViolationWithTemplate("Quantity cannot be negative")
                    .addPropertyNode("quantity").addConstraintViolation();
            isValid = false;
        }

        if (dto.getBrand() != null && dto.getBrand().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Brand cannot be blank")
                    .addPropertyNode("brand").addConstraintViolation();
            isValid = false;
        }

        if (dto.getSku() != null && dto.getSku().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("SKU cannot be blank")
                    .addPropertyNode("sku").addConstraintViolation();
            isValid = false;
        }

        if (dto.getCategory() != null && dto.getCategory().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Category cannot be blank")
                    .addPropertyNode("category").addConstraintViolation();
            isValid = false;
        }

        if (dto.getSubCategory() != null && dto.getSubCategory().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Sub-Category cannot be blank")
                    .addPropertyNode("subCategory").addConstraintViolation();
            isValid = false;
        }

        if (dto.getDiscountedPrice() != null && dto.getDiscountedPrice().compareTo(BigDecimal.ZERO) < 0) {
            context.buildConstraintViolationWithTemplate("Discounted price must be zero or greater")
                    .addPropertyNode("discountedPrice").addConstraintViolation();
            isValid = false;
        }

        // No validation needed for tags and isFeatured — optional fields without restrictions

        return isValid;
    }

}
