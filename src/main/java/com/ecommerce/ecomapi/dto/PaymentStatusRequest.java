package com.ecommerce.ecomapi.dto;

public class PaymentStatusRequest {
    private String paymentIntentId;
    public String getPaymentIntentId() {
        return paymentIntentId;
    }
    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
}
