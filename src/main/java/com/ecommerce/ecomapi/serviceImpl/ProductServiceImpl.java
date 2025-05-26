package com.ecommerce.ecomapi.serviceImpl;

import com.ecommerce.ecomapi.entity.Product;
import com.ecommerce.ecomapi.entity.Rating;
import com.ecommerce.ecomapi.exception.ProductNotFoundException;
import com.ecommerce.ecomapi.repository.ProductRepo;
import com.ecommerce.ecomapi.repository.RatingRepo;
import com.ecommerce.ecomapi.service.CloudinaryService;
import com.ecommerce.ecomapi.service.ProductService;
import com.ecommerce.ecomapi.utility.ObjectIdUtils;
import com.ecommerce.ecomapi.utility.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private RatingRepo ratingRepo;
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public Page<Product> getAll(int page, int size, String sortBy, String order) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size,sort);
       return productRepo.findAll(pageable);
    }

    /* Get all product by seller id*/
    public Page<Product> getAllBySellerId( int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Long sellerId = securityUtil.getCurrentUserId();

        return productRepo.findBySellerId(String.valueOf(sellerId), pageable);
    }

    /* Get product by id */
    @Override
    public Product getProductById(ObjectId id) {
        Product product = productRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product Not Found"));
        return product;
    }

    /* Save product */
    @Override
    public String saveProduct(Product product) {
        try {
            Long sellerId = securityUtil.getCurrentUserId();

            if (product.getTags() != null) {
                List<String> cleanedTags = product.getTags()
                        .stream()
                        .filter(Objects::nonNull)           // remove null tags if any
                        .map(String::trim)                  // trim leading/trailing whitespace
                        .filter(s -> !s.isEmpty())          // remove empty strings
                        .collect(Collectors.toList());
                product.setTags(cleanedTags);
            }

            if (product.getRating() != null && !product.getRating().isEmpty()){
                List<Rating> saveRatings = product.getRating().stream().map(rating -> {
                    if (rating.getId() == null) {
                        rating.setCreatedAt(LocalDateTime.now());
                        return ratingRepo.save(rating);
                    }
                    return  rating;
                }).collect(Collectors.toList());

                product.setRating(saveRatings);
            }
            product.setSellerId(String.valueOf(sellerId));
            product.setCreatedAt(LocalDateTime.now());
            product.setTotalReviews(0);
            product.setAvailable(true);

            Product savedProduct = productRepo.save(product);
            log.info("Product saved: {}", savedProduct);

            return ObjectIdUtils.toString(savedProduct.getId());

        } catch (Exception e) {
            log.error("Something went wrong during product saving!", e);
            return null;
        }
    }

    /* Update product */
    @Transactional
    @Override
    public boolean updateProduct(ObjectId id, Product product) {
        System.out.println(id);
        Product  checkedProduct = getProductById(id);
        try {

            if (product.getProductName() != null) {
                checkedProduct.setProductName(product.getProductName());
            }
            if (product.getDescription() != null) {
                checkedProduct.setDescription(product.getDescription());
            }
            if (product.getPrice() != null) {
                checkedProduct.setPrice(product.getPrice());
            }
            if (product.getQuantity() != null) {
                checkedProduct.setQuantity(product.getQuantity());
            }
            if (product.getBrand() != null) {
                checkedProduct.setBrand(product.getBrand());
            }
            if (product.getSku() != null) {
                checkedProduct.setSku(product.getSku());
            }
            if (product.getCategory() != null) {
                checkedProduct.setCategory(product.getCategory());
            }
            if (product.getSubCategory() != null) {
                checkedProduct.setSubCategory(product.getSubCategory());
            }
            if (product.getAvailable() != null) {
                checkedProduct.setAvailable(product.getAvailable());
            }
            if (product.getRating() != null) {
                checkedProduct.setRating(product.getRating());
            }
            if (product.getTotalReviews() != null) {
                checkedProduct.setTotalReviews(product.getTotalReviews());
            }
            if (product.getMainImageUrl() != null) {
                checkedProduct.setMainImageUrl(product.getMainImageUrl());
            }
            if (product.getAdditionalImageUrls() != null) {
                checkedProduct.setAdditionalImageUrls(product.getAdditionalImageUrls());
            }
            if (product.getTags() != null) {
                checkedProduct.setTags(product.getTags());
            }
            if (product.getDiscountedPrice() != null) {
                checkedProduct.setDiscountedPrice(product.getDiscountedPrice());
            }
            if (product.getIsFeatured() != null) {
                checkedProduct.setIsFeatured(product.getIsFeatured());
            }
            saveProduct(checkedProduct);
            return true;
        }catch (Exception e){
            log.error("Something went to wrong while updating products! {}", e );
            return false;
        }
    }

    /* Delete product by id */
    @Transactional
    @Override
    public boolean deleteProductById(ObjectId id) {

       Product product =  productRepo.findById(id).orElseThrow(() -> new ProductNotFoundException("Product Not Found"));

       if (!product.getSellerId().equals(securityUtil.getCurrentUserId())){
           throw new AccessDeniedException("You are not authorized to delete this product.");
       }

       if (product != null ){
       List<Rating> ratings = product.getRating();
    if (ratings != null && !ratings.isEmpty()){
        ratingRepo.deleteAll(ratings);
    }
           productRepo.deleteById(id);
           return true;
       }
        return false;
    }

    /* Delete products by seller id */
    @Transactional
    @Override
    public boolean deleteProductBySellerId(String sellerId){
        List<Product> products = productRepo.findBySellerId(sellerId);

        if (products == null || products.isEmpty()) {
            return false; // No products to delete
        }

            for (Product product:products){
                List<Rating> ratings = product.getRating();

                if (ratings != null && !ratings.isEmpty()){
                    ratingRepo.deleteAll(ratings);
                }

                productRepo.deleteById(product.getId());
            }

        return true;
    }

    /* Delete all additional Images by product id */
    public boolean deleteAllAdditionalImagesByProductId(ObjectId productId) {
       try {
           Product product = getProductById(productId);

           if (!product.getSellerId().equals(securityUtil.getCurrentUserId())){
               throw new AccessDeniedException("You are not authorized to delete this product.");
           }

           List<String>  additionalImagesurl = product.getAdditionalImageUrls();
           if (additionalImagesurl != null && !additionalImagesurl.isEmpty()){
               for (String public_Id : additionalImagesurl){
                   cloudinaryService.deleteImageThroughPublicId(public_Id);
               }
               product.setAdditionalImageUrls(new ArrayList<>());
               updateProduct(productId, product);
               return  true;
           }

           return false;
       }catch (Exception e){
           log.error("Got error while deleting additional images. ", e);
           return false;
       }
    }

    /* Delete all product */
    @Transactional
    @Override
    public boolean deleteAllProduct() {
        try {
            productRepo.deleteAll();
            ratingRepo.deleteAll();
            return true;
        }catch (Exception e) {
            log.error("Something went to wrong while deleting all product ", e);
            return false;
        }
    }



    /* Get product by category wise */
    public Page<Product> getProductsByCategory(String category,int page, int size, String sortBy, String order) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size,sort);
        return productRepo.findByCategoryIgnoreCase(category.trim(),pageable);
    }

    /* Get product by category and sub-category wise */
    public Page<Product> getProductsByCategoryAndSubCategory(String category, String subCategory,int page, int size, String sortBy, String order) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size,sort);

        return productRepo.findByCategoryAndSubCategoryAllIgnoreCase(category.trim(), subCategory.trim(), pageable);
    }

    /* Get product by name */
    public Page<Product> getProductsByProductName(String productName,  int page, int size, String sortBy, String order) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size,sort);

        return productRepo.findByProductNameIgnoreCase(productName.trim(),pageable);
    }

    // Optional: for fuzzy/partial match
    public Page<Product> searchProductsByName(String name, int page, int size, String sortBy, String order) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size,sort);

        String regex = ".*" + name.trim() + ".*";
        return productRepo.findByProductNameRegexIgnoreCase(regex,pageable);
    }

    /* Find product by exact tag */
    public Page<Product> findProductsByExactTags(List<String> inputTags, int page, int size, String sortBy, String order) {
        // Clean and trim input tags
        List<String> cleanedTags = inputTags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();

        // Sort configuration
        Sort sort = "desc".equalsIgnoreCase(order)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Get all candidates with any matching tag
        Page<Product> candidates = productRepo.findByTagsIn(cleanedTags, pageable);

        // Filter products whose tags exactly match the given list (ignoring order)
        List<Product> exactMatches = candidates.getContent().stream()
                .filter(p -> {
                    List<String> productTags = p.getTags();
                    return productTags != null &&
                            productTags.size() == cleanedTags.size() &&
                            new HashSet<>(productTags).equals(new HashSet<>(cleanedTags));
                })
                .toList();

        // Create new Page from filtered results
        return new PageImpl<>(exactMatches, pageable, exactMatches.size());
    }

    /* Find product by any tag present in list */
    public Page<Product> findProductsByAnyTags(List<String> tags, int page, int size,String sortBy, String order) {
        // Trim each tag before searching
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        List<String> cleanedTags = tags.stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size,sort);
        // returns products that contain any of the given tags
        return productRepo.findByTagsIn(cleanedTags, pageable);
    }

}
