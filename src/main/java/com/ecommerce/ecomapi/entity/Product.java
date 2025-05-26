package com.ecommerce.ecomapi.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    private ObjectId id;

    @Indexed
    private String sellerId;

    @TextIndexed
    private String productName;

    @Column(length = 1000)
    private String description;

    private BigDecimal price;

    private Integer quantity;

    @Indexed
    private String brand;

    private String sku; // Stock Keeping Unit (unique identifier)

    private String category;

    private String subCategory;

    private Boolean available;

    @DBRef(lazy = false)
    private List<Rating> rating;

    private Integer totalReviews;

    private LocalDateTime createdAt;
    // Stores Cloudinary public_id or image URL
    private String mainImageUrl;

    private List<String> additionalImageUrls;

    private List<String> tags;

    private BigDecimal discountedPrice;

    @Indexed
    private Boolean isFeatured;

}
