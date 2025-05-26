package com.ecommerce.ecomapi.entity;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ratings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rating {
    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    private ObjectId id;
    private String userId;
    private String userName;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;
}
