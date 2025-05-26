package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.dto.PaymentIntentRequest;
import com.ecommerce.ecomapi.dto.PaymentIntentResponseDto;
import com.ecommerce.ecomapi.dto.PaymentStatusRequest;
import com.ecommerce.ecomapi.entity.Order;
import com.ecommerce.ecomapi.entity.OrderItem;
import com.ecommerce.ecomapi.entity.Product;
import com.ecommerce.ecomapi.enums.OrderStatus;
import com.ecommerce.ecomapi.repository.OrderRepo;
import com.ecommerce.ecomapi.service.EmailService;
import com.ecommerce.ecomapi.service.PaymentService;
import com.mongodb.client.result.UpdateResult;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private EmailService emailService;

    public PaymentIntentResponseDto createPaymentIntent(PaymentIntentRequest request) throws StripeException {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(request.getAmount())
                        .setCurrency("brl")
                        .setDescription("Order Payment")
                        .putMetadata("order_id", String.valueOf(request.getOrderId()))
                        .putMetadata("user_id", String.valueOf(request.getUserId()))
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                        .build()
                        )
                        .build();


        PaymentIntent intent = PaymentIntent.create(params);

        PaymentIntentResponseDto responseDto = new PaymentIntentResponseDto();
        responseDto.setPaymentIntentId(intent.getId());
        responseDto.setClientSecret(intent.getClientSecret());

        return responseDto;
    }

    public Map<String, Object> verifyPayment(PaymentStatusRequest request) throws Exception {
        Map<String, Object> response = new HashMap<>();

        try {
            String fullId = request.getPaymentIntentId();
            String paymentIntentId = fullId.contains("_secret") ? fullId.split("_secret")[0] : fullId;

            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            response.put("status", intent.getStatus());

            if (!"succeeded".equals(intent.getStatus())) {
                response.put("message", "Payment not successful.");
                return response;
            }

            Long orderId = Long.valueOf(intent.getMetadata().get("order_id"));
            Order order = orderRepo.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!"succeeded".equalsIgnoreCase(order.getPaymentStatus())) {

                // Check stock availability before updating
                for (OrderItem item : order.getOrderItems()) {
                    Query query = new Query(Criteria.where("_id").is(item.getProductId())
                            .and("quantity").gte(item.getQuantity()));
                    boolean enoughStock = mongoTemplate.exists(query, Product.class);
                    if (!enoughStock) {
                        response.put("message", "Insufficient stock for product: " + item.getProductId());
                        return response;
                    }
                }
                System.out.println("Payment set");
                // Update order
                order.setPaymentStatus("succeeded");
                order.setPaymentDate(LocalDateTime.now());
                order.setOrderStatus(OrderStatus.PROCESSING);
                order.setStatus("CONFIRMED");
                order.setDeliveryDate(Date.from(
                        LocalDateTime.now().plusDays(5).atZone(ZoneId.systemDefault()).toInstant()
                ));
                String emailBody = emailService.buildOrderConfirmationEmail(order);
                emailService.sendHtmlEmail(order.getUser().getEmail(), "Your Order is Confirmed", emailBody);
                orderRepo.save(order);

                // Decrease stock
                for (OrderItem item : order.getOrderItems()) {
                    Query query = new Query(Criteria.where("_id").is(item.getProductId()));
                    Update update = new Update().inc("quantity", -item.getQuantity());
                    mongoTemplate.updateFirst(query, update, Product.class);
                }
            }

            response.put("amount", intent.getAmount());
            response.put("currency", intent.getCurrency());
            response.put("description", intent.getDescription());
            response.put("orderId", orderId);
            response.put("userId", intent.getMetadata().get("user_id"));
            response.put("message", "Payment verified successfully.");
            return response;

        } catch (Exception e) {
            response.put("message", "Payment verification failed: " + e.getMessage());
            return response;
        }
    }

}
