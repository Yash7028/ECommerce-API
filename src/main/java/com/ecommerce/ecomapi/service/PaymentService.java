package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.dto.PaymentIntentRequest;
import com.ecommerce.ecomapi.dto.PaymentIntentResponseDto;
import com.ecommerce.ecomapi.dto.PaymentStatusRequest;
import com.stripe.exception.StripeException;

import java.util.Map;

public interface PaymentService {
    public PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequest request) throws StripeException;
    public Map<String, Object> verifyPayment(PaymentStatusRequest request) throws Exception;
}
