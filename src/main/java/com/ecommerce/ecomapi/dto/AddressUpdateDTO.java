package com.ecommerce.ecomapi.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressUpdateDTO {
    @Size(max = 255, message = "Street cannot exceed 255 characters")
    private String street;

    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Zip code cannot exceed 20 characters")
    private String zip;

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 50, message = "Address type cannot exceed 50 characters")
    private String addressType;
}
