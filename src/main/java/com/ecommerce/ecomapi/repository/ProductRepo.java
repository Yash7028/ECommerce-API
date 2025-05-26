package com.ecommerce.ecomapi.repository;

import com.ecommerce.ecomapi.entity.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends MongoRepository<Product, ObjectId> {
    List<Product> findBySellerId(String sellerId);

    @Override
    Page<Product> findAll(Pageable pageable);

    Page<Product> findBySellerId(String sellerId, Pageable pageable);

    Page<Product> findByCategoryIgnoreCase(String category,Pageable pageable);
    Page<Product> findByCategoryAndSubCategoryAllIgnoreCase(String category, String subCategory,Pageable pageable);
    // Case-insensitive search by productName
    Page<Product> findByProductNameIgnoreCase(String productName,Pageable pageable);

    // OR: partial match using regex (optional)
    Page<Product> findByProductNameRegexIgnoreCase(String regex,Pageable pageable);
//    List<Product> findByTags(List<String> tags);

    // Matches products that contain any of the specified tags (more useful)
    Page<Product> findByTagsIn(List<String> tags, Pageable pageable);

//    // Optional: Matches products that contain all the specified tags
//    @org.springframework.data.mongodb.repository.Query("{ 'tags': { $all: ?0 } }")
//    List<Product> findByTagsAll(List<String> tags);

    Optional<Product> findByRatingId(ObjectId ratingId);

}
