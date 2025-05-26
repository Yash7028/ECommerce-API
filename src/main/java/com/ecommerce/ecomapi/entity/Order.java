package com.ecommerce.ecomapi.entity;

import com.ecommerce.ecomapi.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name= "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"orderItems","user", "billingAddress", "shippingAddress"})

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> orderItems;

    private BigDecimal totalAmount;
    private BigDecimal discount;
    private  BigDecimal finalAmount;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentIntentId;
    private String clientSecret;
    private Boolean refunded = false;
    private Date refundDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_address_id")
//    @JsonManagedReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler","user"})
    private Address billingAddress;

//    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
//@JsonManagedReference
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler","user"})
    private Address shippingAddress;

    private LocalDateTime createdAt;

    private LocalDateTime paymentDate;

    private Date deliveryDate;

    private LocalDateTime deliveredAt;

    private String cancelledReason;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Matches the column name in the database
//    @JsonBackReference
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler","orders", "addresses"})
    @JsonIgnore
    private User user;


}
