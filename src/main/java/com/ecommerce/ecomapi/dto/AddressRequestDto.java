package com.ecommerce.ecomapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequestDto {
    @NotBlank(message = "Street is mandatory")
    @Size(max = 255, message = "Street cannot exceed 255 characters")
    private String street;

    @NotBlank(message = "City is mandatory")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "State is mandatory")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @NotBlank(message = "Zip code is mandatory")
    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    private String zip;

    @NotBlank(message = "Country is mandatory")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @NotBlank(message = "Address type is mandatory")
    @Size(max = 50, message = "Address type cannot exceed 50 characters")
    private String addressType;
}
