package com.ecommerce.ecomapi.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class MobileNumberValidator implements ConstraintValidator<ValidMobileNumber, String> {

    @Override
    public void initialize(ValidMobileNumber validMobileNumber) {

    }

    private static final String MOBILE_NUMBER_PATTERN = "^[6-9]\\d{9}$";

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        return phoneNumber != null && phoneNumber.matches(MOBILE_NUMBER_PATTERN);
    }
}
