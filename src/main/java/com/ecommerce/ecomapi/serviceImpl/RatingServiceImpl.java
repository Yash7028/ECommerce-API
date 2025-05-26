package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.entity.Product;
import com.ecommerce.ecomapi.entity.Rating;
import com.ecommerce.ecomapi.entity.User;
import com.ecommerce.ecomapi.exception.ProductNotFoundException;
import com.ecommerce.ecomapi.exception.RatingNotFoundException;
import com.ecommerce.ecomapi.repository.ProductRepo;
import com.ecommerce.ecomapi.repository.RatingRepo;
import com.ecommerce.ecomapi.service.RatingService;
import com.ecommerce.ecomapi.service.UserService;
import com.ecommerce.ecomapi.utility.ObjectIdUtils;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.soap.SOAPBinding;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Service
public class RatingServiceImpl implements RatingService {
    @Autowired
    private RatingRepo ratingRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    /* Get all ratings */
    @Override
    public List<Rating> getAll() {
        List<Rating> ratingList = ratingRepo.findAll();
        if (ratingList != null && !ratingList.isEmpty()){
            return ratingList;
        }
        return null;
    }

    /* Get rating of product */
    public List<Rating> getRatingsByProductId(ObjectId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID must not be null");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        List<Rating> ratings = product.getRating();
        if (ratings == null || ratings.isEmpty()) {
            throw new ProductNotFoundException("No ratings found for this product.");
        }
        return ratings;
    }

    /* Get single rating by id */
    @Override
    public Rating getByRatingId(ObjectId id) {
        Rating rating = ratingRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Rating is not present"));
        return rating;
    }

    /* Create rating */
    public boolean createRating(String productId,Rating rating){
        Long userId = securityUtil.getCurrentUserId();

        User user = userService.getUserById(userId);
       Product product =  productRepo.findById(ObjectIdUtils.toObjectId(productId))
               .orElseThrow(() -> new ProductNotFoundException("Product not found"));

       rating.setUserId(String.valueOf(userId));
       rating.setUserName(user.getName());
       rating.setCreatedAt(LocalDateTime.now());

       Rating savedRating = ratingRepo.save(rating);
        System.out.println("Rating id: "+savedRating.getId());
       if (product.getRating() == null){
           product.setRating(new ArrayList<>());
       }

       product.getRating().add(savedRating);
       product.setTotalReviews(product.getTotalReviews() + 1);

       productRepo.save(product);

       return true;
    }

    /* Update rating by id*/
    @Transactional
    @Override
    public boolean updateRatingById( ObjectId id,Rating updatedRating) {
        Rating existingRating = getByRatingId(id);

        if (existingRating == null) {
            return false;
        }

        // Authorization check: Only the creator can update
        if (!existingRating.getUserId().equals(String.valueOf(securityUtil.getCurrentUserId()))) {
            return false;
        }

        // Update fields only if provided
        if (updatedRating.getStars() != -1) {
            existingRating.setStars(updatedRating.getStars());
        }
        if (updatedRating.getComment() != null && !updatedRating.getComment().trim().isEmpty()) {
            existingRating.setComment(updatedRating.getComment());
        }

        ratingRepo.save(existingRating);
        return true;
    }

    /* Delete rating by id */
    @Transactional
    @Override
    public boolean deleteRatingById(ObjectId ratingId) {
        boolean isAdmin = securityUtil.getCurrentUserAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        // Find rating
        Rating rating = ratingRepo.findById(ratingId)
                .orElseThrow(() -> new RatingNotFoundException("Rating not found"));

        // Authorization check
        if (!isAdmin && !rating.getId().equals(securityUtil.getCurrentUserId())) {
            throw new AccessDeniedException("You are not authorized to delete this rating.");
        }

        // Find the product that contains this rating
        Product product = productRepo.findByRatingId(ratingId)
                .orElseThrow(() -> new ProductNotFoundException("Product containing this rating not found"));

        // Remove the rating from product
        List<Rating> updatedRatings = new ArrayList<>(product.getRating());
        updatedRatings.removeIf(r -> r.getId().equals(ratingId));
        product.setRating(updatedRatings);

        // Save and delete
        productRepo.save(product);
        ratingRepo.deleteById(ratingId);
        return true;
    }

    /* Delete all rating */
    @Transactional
    @Override
    public boolean deleteAllRating() {
        List<Product> products = productRepo.findAll();
        if (products != null && !products.isEmpty()){

            for (Product product : products) {
                List<Rating> ratings = product.getRating();
                if (ratings != null && !ratings.isEmpty()) {
                    // Delete each rating from the ratingRepo
                    for (Rating rating : ratings) {
                        ratingRepo.deleteById(rating.getId());
                    }
                    // Clear the rating list from the product and save it
                    product.setRating(List.of());
                    product.setTotalReviews(0);
                    productRepo.save(product); // Or productRepo.save(product);
                }
            }

            ratingRepo.deleteAll();
            return  true;
        }

        return false;
    }
}
