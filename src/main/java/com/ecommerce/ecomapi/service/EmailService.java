package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.entity.Order;

public interface EmailService {
    public void sendEmail(String to, String subject, String body);
    public String buildOrderConfirmationEmail(Order order);
    public void sendHtmlEmail(String to,String subject,String htmlContent);

    public String buildOrderCancellationEmail(Order order, String cancellationReason);
}
