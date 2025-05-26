package com.ecommerce.ecomapi.exception;

public class OrderIsAlreadyPlacedException extends RuntimeException{
    public OrderIsAlreadyPlacedException(String message) {
        super(message);
    }
}
