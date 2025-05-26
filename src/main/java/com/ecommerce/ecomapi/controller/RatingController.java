package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.RequestCreateRatingDto;
import com.ecommerce.ecomapi.entity.Rating;
import com.ecommerce.ecomapi.exception.ProductNotFoundException;
import com.ecommerce.ecomapi.service.RatingService;
import com.ecommerce.ecomapi.utility.ObjectIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rating")
@Tag(
        name = "06. Rating APIs",
        description = "Endpoints for managing and accessing product ratings, including create, update, delete, and fetch operations."
)
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private ModelMapper modelMapper;

    /* Get all rating */
    @Operation(summary = "Get all ratings", description = "Get ratings of all products. Only Admin can access.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRating(){
        List<Rating> rating = ratingService.getAll();
        if (rating != null){
            return new ResponseEntity<>(rating, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("There is no rating data found ");
    }

    /* Get all rating of specific product */
    @Operation(summary = "Get rating", description = "Get all rating of particular product. Not restricted for any one.")
    @GetMapping("/get-by-id/{productId}/productId")
    public ResponseEntity<?> getRatingsByProductId(@PathVariable String productId) {
        try {
            if (!ObjectId.isValid(productId)) {
                return ResponseEntity.badRequest().body("Invalid product ID format");
            }

            List<Rating> ratings = ratingService.getRatingsByProductId(ObjectIdUtils.toObjectId(productId));
            return ResponseEntity.ok(ratings);

        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /* Get rating by id */
    @Operation(summary = "Get rating", description = "Get single rating. Not restricted for any one.")
    @GetMapping("/get-by-id/{ratingId}/ratingId")
    public ResponseEntity<?> getRatingById(@PathVariable String ratingId){
        try {
            if (!ObjectId.isValid(ratingId)) {
                return ResponseEntity.badRequest().body("Invalid product ID format");
            }

            Rating rating = ratingService.getByRatingId(ObjectIdUtils.toObjectId(ratingId));
            if (rating != null){
                return ResponseEntity.ok(rating);
            }
            String response = "Rating is not present with this : " + ratingId;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @Operation(summary = "Create rating ", description = "Create rating and get response status as 200.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create/{productId}")
    public ResponseEntity<?> createRating(@PathVariable String productId,@RequestBody RequestCreateRatingDto rating){
        if (!ObjectId.isValid(productId)) {
            return ResponseEntity.badRequest().body("Invalid product ID format");
        }

        if(ratingService.createRating(productId, modelMapper.map(rating, Rating.class))){
            return ResponseEntity.ok("Commented successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create rating");
    }

    /* Update rating only creator can be edit rating. */
    @Operation(summary = "Update rating ", description = "only creator can be edit rating.")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update/{ratingId}")
    public ResponseEntity<?> updateRating(@PathVariable String ratingId, @RequestBody RequestCreateRatingDto  rating){
        try {
            boolean isUpdated = ratingService.updateRatingById(ObjectIdUtils.toObjectId(ratingId),modelMapper.map( rating, Rating.class));

            if (isUpdated) {
                return ResponseEntity.ok("Rating updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to update this rating.");
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid rating ID.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
        }
    }

    /* Delete rating by */
    @Operation(summary = "Delete rating ", description = "Delete rating by rating id. Only admin and creator user can delete rating.")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/delete/{ratingId}/ratingId")
    public ResponseEntity<?> deleteById(@PathVariable String ratingId){
        if (!ObjectId.isValid(ratingId)) {
            return ResponseEntity.badRequest().body("Invalid product ID format");
        }

        boolean isDeleted = ratingService.deleteRatingById(ObjectIdUtils.toObjectId(ratingId));
        if (isDeleted){
            return ResponseEntity.ok("Rating Deleted successfully ");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Rating is not deleted yet.");
    }

    @Operation(summary = "Delete all ratings ", description = "Delete all rating. Only admin can do this.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAll(){
        boolean isDeleted = ratingService.deleteAllRating();
        if (isDeleted){
            return new ResponseEntity<>("Rating Deleted successfully ", HttpStatus.OK);
        }
        return new ResponseEntity<>("Rating is not Delete ", HttpStatus.FORBIDDEN);
    }


}
