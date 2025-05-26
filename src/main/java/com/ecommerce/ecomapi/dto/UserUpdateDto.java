package com.ecommerce.ecomapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    private String name;
    private String password;
    private String phoneNumber;
    private String profilePic;
    private String gender;
}
