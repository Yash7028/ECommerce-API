package com.ecommerce.ecomapi.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductUpdateValidator.class)
public @interface ValidProductUpdate {
    String message() default "Invalid product update data";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
