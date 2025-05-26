package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.entity.Rating;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepo extends MongoRepository<Rating, ObjectId> {
    void deleteByUserId(String userId);

}
