package com.ecommerce.ecomapi.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productId;
    private String name;
    private int quantity;
    private BigDecimal totalPrice ;
    private BigDecimal discountedPrice;

    private String mainImage;
    @ElementCollection
    private List<String> additionalImages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "order_id")
//    @JsonBackReference
//    @JsonIgnoreProperties("orderItems")
    @JsonIgnore
    private Order order;
}
