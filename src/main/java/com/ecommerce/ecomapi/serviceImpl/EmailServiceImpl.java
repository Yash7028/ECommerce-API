package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.entity.Address;
import com.ecommerce.ecomapi.entity.Order;
import com.ecommerce.ecomapi.entity.OrderItem;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender javaMailSender;
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);

            javaMailSender.send(mail);
        }catch (Exception e){
            log.error("Exception occur while send email : ", e);
        }
    }

    public void sendHtmlEmail(String to,String subject,String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        }catch (Exception e){
            log.error("Error occur while sending message : ", e);
        }

    }

    public String buildOrderConfirmationEmail(Order order) {
        User user = order.getUser();
        Address addressShipping = order.getShippingAddress();
        Address addressBilling = order.getBillingAddress();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedOrderDate = order.getCreatedAt().format(dateFormatter);
        String formattedDeliveryDate = order.getDeliveryDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .format(dateFormatter);

        StringBuilder sb = new StringBuilder();

        // Header (Keep color)
        sb.append("<h2 style='color:#2E86C1;'>Thank you for your order, ").append(user.getName()).append("!</h2>");
        sb.append("<p style='color:#000;'>Your order has been confirmed. Below are your order details:</p>");

        // Order Info
        sb.append("<p style='color:#000;'><strong>Order ID:</strong> ").append(order.getId()).append("<br>");
        sb.append("<strong>Order Date:</strong> ").append(formattedOrderDate).append("</p>");

        // Shipping Info
        sb.append("<h3 style='color:#000;'>Shipping Details:</h3>");
        sb.append("<p style='color:#000;'><strong>Name:</strong> ")
                .append(user.getName()).append("<br>");
        sb.append("<strong>Mobile:</strong> ")
                .append(user.getPhoneNumber()).append("<br>");
        sb.append("<strong>Shipping Address:</strong> ")
                .append(addressShipping.getStreet()).append(", ")
                .append(addressShipping.getCity()).append(", ")
                .append(addressShipping.getState()).append(" - ")
                .append(addressShipping.getZip()).append("<br>");
        sb.append("<strong>Billing Address:</strong> ")
                .append(addressBilling.getStreet()).append(", ")
                .append(addressBilling.getCity()).append(", ")
                .append(addressBilling.getState()).append(" - ")
                .append(addressBilling.getZip()).append("</p>");

        // Order Summary
        sb.append("<h3 style='color:#000;'>Order Summary:</h3>");
        sb.append("<table style='border-collapse: collapse; width: 100%; color: #000;'>")
                .append("<thead><tr>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Product</th>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Qty</th>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Price</th>")
                .append("</tr></thead><tbody>");

        for (OrderItem item : order.getOrderItems()) {
            sb.append("<tr>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(item.getName()).append("</td>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>₹").append(item.getDiscountedPrice()).append("</td>")
                    .append("</tr>");
        }

        sb.append("</tbody></table>");

        // Payment Info
        sb.append("<p style='color:#000;'><strong>Total Amount:</strong> ₹").append(order.getFinalAmount()).append("</p>");
        sb.append("<p style='color:#000;'><strong>Payment Method:</strong> ").append(order.getPaymentMethod()).append("</p>");
        sb.append("<p style='color:#000;'><strong>Expected Delivery:</strong> ").append(formattedDeliveryDate).append("</p>");

        // Track Order Button
        sb.append("<br><a href='https://yourdomain.com/track-order/")
                .append(order.getId())
                .append("' style='display: inline-block; padding: 10px 20px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px;'>Track Your Order</a>");

        // Footer
        sb.append("<br><br><p style='color:#000;'>If you have any questions, feel free to contact us.</p>");
        sb.append("<p style='color:#555;'>--<br>Smart Contact Manager Team</p>");

        return sb.toString();
    }


    public String buildOrderCancellationEmail(Order order, String cancellationReason) {
        User user = order.getUser();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String formattedOrderDate = order.getCreatedAt().format(dateFormatter);

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("<h2 style='color:#E74C3C;'>Your order has been cancelled, ").append(user.getName()).append(".</h2>");
        sb.append("<p style='color:#000;'>We're sorry to inform you that your order has been cancelled. Below are the details:</p>");

        // Order Info
        sb.append("<p style='color:#000;'><strong>Order ID:</strong> ").append(order.getId()).append("<br>");
        sb.append("<strong>Order Date:</strong> ").append(formattedOrderDate).append("</p>");

        // Cancellation Reason
        sb.append("<p style='color:#000;'><strong>Reason for cancellation:</strong> ").append(cancellationReason != null ? cancellationReason : "Not specified").append("</p>");

        // Order Summary
        sb.append("<h3 style='color:#000;'>Order Summary:</h3>");
        sb.append("<table style='border-collapse: collapse; width: 100%; color: #000;'>")
                .append("<thead><tr>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Product</th>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Qty</th>")
                .append("<th style='border: 1px solid #ddd; padding: 8px;'>Price</th>")
                .append("</tr></thead><tbody>");

        for (OrderItem item : order.getOrderItems()) {
            sb.append("<tr>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(item.getName()).append("</td>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>").append(item.getQuantity()).append("</td>")
                    .append("<td style='border: 1px solid #ddd; padding: 8px;'>₹").append(item.getDiscountedPrice()).append("</td>")
                    .append("</tr>");
        }

        sb.append("</tbody></table>");

        // Payment Info (if refund applicable)
        sb.append("<p style='color:#000;'><strong>Total Amount:</strong> ₹").append(order.getFinalAmount()).append("</p>");
        sb.append("<p style='color:#000;'><strong>Payment Method:</strong> ").append(order.getPaymentMethod()).append("</p>");

        // Footer
        sb.append("<br><p style='color:#000;'>If you have any questions or need assistance, please contact us.</p>");
        sb.append("<p style='color:#555;'>--<br>Smart Contact Manager Team</p>");

        return sb.toString();
    }


}
