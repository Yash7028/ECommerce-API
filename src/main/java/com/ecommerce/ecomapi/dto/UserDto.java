package com.ecommerce.ecomapi.dto;

import com.ecommerce.ecomapi.enums.Gender;
import com.ecommerce.ecomapi.enums.Roles;
import com.ecommerce.ecomapi.validator.ValidMobileNumber;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto{

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @ValidMobileNumber
    private String phoneNumber;

    @NotNull(message = "Gender is required")
    private String gender;
    private List<Roles> roles;
}
//@Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number!")