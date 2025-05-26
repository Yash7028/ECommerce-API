package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.entity.Rating;
import org.bson.types.ObjectId;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RatingService {
    public List<Rating> getAll();
    public Rating getByRatingId(ObjectId id);

    public List<Rating> getRatingsByProductId(ObjectId productId);
    public boolean createRating(String productId,Rating rating);
    public boolean updateRatingById(ObjectId ratingId,Rating rating);
    public boolean deleteRatingById(ObjectId ratingId);
    public boolean deleteAllRating();
}
