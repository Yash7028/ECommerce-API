package com.ecommerce.ecomapi.dto;

import lombok.Data;

@Data
public class AddressDto {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String addressType;
    private boolean isSaved;

}
