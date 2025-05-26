package com.ecommerce.ecomapi.exception;

public class AuthorizationDeniedException extends RuntimeException{
    public AuthorizationDeniedException(String message) {
        super(message);
        System.out.println("Authorization denial exception");
    }
}
