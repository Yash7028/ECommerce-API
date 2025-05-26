package com.ecommerce.ecomapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentIntentResponseDto {
   private String paymentIntentId;
   private String clientSecret;
}
