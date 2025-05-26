package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.PaymentStatusRequest;
import com.ecommerce.ecomapi.repository.OrderRepo;
import com.ecommerce.ecomapi.service.PaymentService;
import com.ecommerce.ecomapi.serviceImpl.PaymentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "*")
@Tag(name = "08. Payment Controller",
        description = "Manages all payment-related verification.")
public class PaymentController {

    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private PaymentService paymentService;

    /*Verify payment with the help intentId*/
    @Operation(
            summary = "Verify Stripe payment status",
            description = "Verifies the status of a Stripe payment after the client completes the payment process. Accessible only to users with the USER role.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentStatusRequest request) throws Exception {
        Map<String, Object> response = paymentService.verifyPayment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
