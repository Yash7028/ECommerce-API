package com.ecommerce.ecomapi.controller;

import com.ecommerce.ecomapi.dto.ProductDTO;
import com.ecommerce.ecomapi.dto.ProductUpdateDTO;
import com.ecommerce.ecomapi.entity.Product;
import com.ecommerce.ecomapi.service.CloudinaryService;
import com.ecommerce.ecomapi.service.ProductService;
import com.ecommerce.ecomapi.utility.CategoryDictionary;
import com.ecommerce.ecomapi.utility.ObjectIdUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ecommerce.ecomapi.utility.CategoryDictionary.isValidCategory;
import static com.ecommerce.ecomapi.utility.CategoryDictionary.isValidSubCategory;

@RestController
@RequestMapping("/product")
@Slf4j
@Validated
@Tag(
        name = "04. Product Controller",
        description = "Handles all operations related to products including creation, retrieval, update, deletion, filtering, and searching of product items."
)
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private ModelMapper modelMapper;

    @Operation(
            summary = "Get all categories and subcategories",
            description = "Returns a map of all main categories and their respective subcategories"
    )
    @GetMapping("/categories")
    public ResponseEntity<Map<String, java.util.List<String>>> getAllCategories() {
        return ResponseEntity.ok(CategoryDictionary.CATEGORY_DICTIONARY);
    }

    /* Get all product */
    @Operation(
            summary = "Get all products",
            description = "Retrieves a list of all available products. Returns 204 No Content if no products are found."
    )
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllProduct(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                           @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Page<Product> product = productService.getAll(page, size, sortBy, direction);
        if (product != null && !product.isEmpty()) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Get all products by seller ID",
            description = "Returns a paginated and sorted list of products for the specified seller."
    )

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/get-all/by-seller")
    public ResponseEntity<?> getAllProductsBySellerId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Page<Product> products = productService.getAllBySellerId(page, size, sortBy, direction);

        if (products != null && !products.isEmpty()) {
            return new ResponseEntity<>(products, HttpStatus.OK);
        }

        return new ResponseEntity<>("No products found for current seller id.", HttpStatus.NO_CONTENT);
    }


    /* Get product by exact tags */
    @Operation(
            summary = "Get products by exact matching tags",
            description = "Returns a paginated and sorted list of products that exactly match all specified tags. Tags list must not be empty."
    )
    @GetMapping("/get-by/exact-tags")
    public ResponseEntity<?> getProductsByExactTags(@RequestParam List<String> tags,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "1") int size,
                                                    @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                                    @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        if (tags == null || tags.isEmpty()) {
            return ResponseEntity.badRequest().body("Tags cannot be empty");
        }

        Page<Product> products = productService.findProductsByExactTags(tags, page, size, sortBy, direction);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found with exact matching tags");
        }
        return ResponseEntity.ok(products);
    }

    /* Get product by any tag */
    @Operation(
            summary = "Get products by any matching tag",
            description = "Returns a paginated and sorted list of products that match any of the specified tags. Tags list must not be empty."
    )
    @GetMapping("/get-by/any-tags")
    public ResponseEntity<?> getProductsByAnyTags(@RequestParam List<String> tags,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "1") int size,
                                                  @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                                  @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        if (tags == null || tags.isEmpty()) {
            return ResponseEntity.badRequest().body("Tags list must not be empty.");
        }

        Page<Product> products = productService.findProductsByAnyTags(tags, page, size, sortBy, direction);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found matching any of the given tags.");
        }

        return ResponseEntity.ok(products);
    }

    /* Find product by exact name */
    @Operation(
            summary = "Get products by exact name",
            description = "Fetches all products that exactly match the given product name. Case-insensitive."
    )
    @GetMapping("/get-by/name")
    public ResponseEntity<?> getProductsByName(@RequestParam String productName,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                               @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Page<Product> products = productService.getProductsByProductName(productName, page, size, sortBy, direction);

        if (products.isEmpty()) {
            return new ResponseEntity<>("No products found with this name", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    // Optional: Fuzzy search endpoint
    @Operation(
            summary = "Search products by keyword",
            description = "Returns a paginated list of products that match the keyword in their name. Supports sorting and pagination."
    )
    @GetMapping("/get-by/search")
    public ResponseEntity<?> searchProducts(@RequestParam String keyword,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                            @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Page<Product> products = productService.searchProductsByName(keyword, page, size, sortBy, direction);

        if (products.isEmpty()) {
            return new ResponseEntity<>("No matching products found", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /* Get product by category */
    @GetMapping("/get-by/category")
    @Operation(
            summary = "Get products by category ",
            description = "Returns a list of products matching the specified category")
    public ResponseEntity<?> getProductsByCategory(@RequestParam String category,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                                   @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        Page<Product> products = productService.getProductsByCategory(category, page, size, sortBy, direction);

        if (products.isEmpty()) {
            return new ResponseEntity<>("No products found for this category", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /* Get product by category and sub-category */
    @Operation(
            summary = "Get products by category and subcategory",
            description = "Returns a list of products matching the specified category and subcategory.")
    @GetMapping("/get-by/category-and-subcategory")
    public ResponseEntity<?> getProductsByCategoryAndSubCategory(@RequestParam String category,
                                                                 @RequestParam String subCategory,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @RequestParam(value = "sortBy", defaultValue = "discountedPrice") String sortBy,
                                                                 @RequestParam(value = "direction", defaultValue = "asc") String direction
    ) {
        Page<Product> products = productService.getProductsByCategoryAndSubCategory(category, subCategory, page, size, sortBy, direction);

        if (products.isEmpty()) {
            return new ResponseEntity<>("No products found for this category and subcategory", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /* Find product by product id */
    //    @CrossOrigin(origins = "*")
    @Operation(
            summary = "Get product by its ID",
            description = "Returns the product details for the specified product ID.")
    @GetMapping("/get-by/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable String productId) {
        try {
            Product product = productService.getProductById(ObjectIdUtils.toObjectId(productId));
            Map<String, Object> responseBody = Map.of(
                    "productData", product
            );
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (Exception e) {

            String response = "There is no product found with the entered id " + productId;
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    /* Create new product */
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "Save a new product",
            description = "Creates a new product with images upload. Main image is required, additional images optional. " +
                    "Validates product fields")
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveProduct(@RequestParam(value = "mainImage") MultipartFile file,
                                         @RequestParam(value = "additionalImages", required = false) MultipartFile[] images,
                                         @ModelAttribute @Valid ProductDTO product, BindingResult result) {

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        String category = product.getCategory() != null ? product.getCategory().trim() : "";
        String subCategory = product.getSubCategory() != null ? product.getSubCategory().trim() : "";

        if (!isValidCategory(category)) {
            return new ResponseEntity<>("Invalid category", HttpStatus.BAD_REQUEST);
        }
        if (!isValidSubCategory(category, subCategory)) {
            return new ResponseEntity<>("Invalid subcategory for selected category", HttpStatus.BAD_REQUEST);
        }

        product.setCategory(category);
        product.setSubCategory(subCategory);

        Product mappedProduct = modelMapper.map(product, Product.class);

        try {
            if (file != null && !file.isEmpty()) {
                String mainImageUrl = cloudinaryService.uploadImage(file);
                mappedProduct.setMainImageUrl(mainImageUrl);
            }

            if (images != null && images.length > 0) {
                List<String> additionalImageUrls = new ArrayList<>();
                for (MultipartFile additionalFile : images) {
                    if (!additionalFile.isEmpty()) {
                        String uploadedUrl = cloudinaryService.uploadImage(additionalFile);
                        additionalImageUrls.add(uploadedUrl);
                    }
                }
                mappedProduct.setAdditionalImageUrls(additionalImageUrls);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Image upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String id = productService.saveProduct(mappedProduct);
        Map<String, String> response = new HashMap<>();
        if (id != null && !id.isEmpty()) {
            response.put("message", "Product saved successfully");
            response.put("product_id", id);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        response.put("message", "Something went wrong while saving the product.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    /*Update all data like product, mainImage, additional Image
     * working :- if you are sending only deleteAdditionalImages list then delete this image and display remain data */
    @Operation(
            summary = "Update an existing product",
            description = "Allows a SELLER to update all details of an existing product including its main image and additional images. " +
                    "Supports replacing the main image, adding new additional images, and deleting selected additional images.")
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping(value = "/update-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@RequestParam String productId,
                                           @Valid @ModelAttribute ProductUpdateDTO productWithUpdatedValues, BindingResult result,
                                           @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
                                           @RequestParam(value = "additionalImages", required = false) MultipartFile[] images,
                                           @RequestParam(value = "deleteAdditionalImages", required = false) List<String> imagesToDelete) throws IOException {

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        Product retrievedProduct = productService.getProductById(ObjectIdUtils.toObjectId(productId));

        String category = productWithUpdatedValues.getCategory();
        String subCategory = productWithUpdatedValues.getSubCategory();

        boolean hasCategory = category != null && !category.trim().isEmpty();
        boolean hasSubCategory = subCategory != null && !subCategory.trim().isEmpty();

        if (hasCategory || hasSubCategory) {

            category = hasCategory ? category.trim() : "";
            subCategory = hasSubCategory ? subCategory.trim() : "";

            if (hasCategory && !isValidCategory(category)) {
                return new ResponseEntity<>("Invalid category", HttpStatus.BAD_REQUEST);
            }

            if (hasCategory && hasSubCategory && !isValidSubCategory(category, subCategory)) {
                return new ResponseEntity<>("Invalid subcategory for selected category", HttpStatus.BAD_REQUEST);
            }

            productWithUpdatedValues.setCategory(category);
            productWithUpdatedValues.setSubCategory(subCategory);
        }

        Product mappedProduct = modelMapper.map(productWithUpdatedValues, Product.class);

        // ✅ Replace Main Image if needed
        if (mainImage != null && !mainImage.isEmpty()) {
            String oldMainImageId = retrievedProduct.getMainImageUrl();
            if (oldMainImageId != null && !oldMainImageId.isEmpty()) {
                cloudinaryService.deleteImageThroughPublicId(oldMainImageId);
            }
            String newMainImageUrl = cloudinaryService.uploadImage(mainImage);
            mappedProduct.setMainImageUrl(newMainImageUrl);
        }

        List<String> updatedAdditionalImageUrls = new ArrayList<>(retrievedProduct.getAdditionalImageUrls());

        if (images != null && images.length > 0) {
            for (MultipartFile additionalFile : images) {
                if (!additionalFile.isEmpty()) {
                    String uploadedUrl = cloudinaryService.uploadImage(additionalFile);
                    updatedAdditionalImageUrls.add(uploadedUrl);
                }
            }
        }

        // ✅ Handle Additional Image Deletion
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            for (String imagePublicId : imagesToDelete) {
                cloudinaryService.deleteImageThroughPublicId(imagePublicId);
                updatedAdditionalImageUrls.remove(imagePublicId); // assumes publicId is stored in DB
            }
        }

        mappedProduct.setAdditionalImageUrls(updatedAdditionalImageUrls);

        boolean isUpdated = productService.updateProduct(ObjectIdUtils.toObjectId(productId), mappedProduct);

        if (isUpdated) {
            return new ResponseEntity<>("Product is updated successfully.", HttpStatus.OK);
        }
        return new ResponseEntity<>("Something went wrong while updating the product!", HttpStatus.BAD_REQUEST);
    }

    /* Update product details */
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/update-details/{productId}")
    @Operation(
            summary = "Update product details",
            description = "Updates details of an existing product by productId. Requires SELLER role. " +
                    "Validates input fields.")
    public ResponseEntity<?> updateDetails(@PathVariable String productId,
                                           @Valid @RequestBody ProductUpdateDTO product,
                                           BindingResult result) {

        if (result.hasErrors()) {
            return new ResponseEntity<>(result.getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST);
        }

        if (product != null) {
            Boolean isUpdated = productService.updateProduct(ObjectIdUtils.toObjectId(productId), modelMapper.map(product, Product.class));
            if (isUpdated) {
                return new ResponseEntity<>("Product details are updated successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Product not with this id ", HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>("Internal server error ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* Delete product by id */
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "Delete a product by ID",
            description = "Deletes a product by its ID. Only the seller who created the product can delete it.")
    @DeleteMapping("/delete-by-id")
    public ResponseEntity<?> deleteProductById(@RequestParam String productId) {
        if (productService.deleteProductById(ObjectIdUtils.toObjectId(productId))) {
            return new ResponseEntity<>("Product is deleted successfully.", HttpStatus.OK);
        }
        String response = "There no any product find with this entered id " + productId;
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    /* Delete all products */
    @Operation(
            summary = "Delete all products",
            description = "Deletes all products from the system. Only accessible to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllProduct() {
        if (productService.deleteAllProduct()) {
            return new ResponseEntity<>("Product is deleted successfully.", HttpStatus.OK);
        }
        return new ResponseEntity<>("There is something error occur while deleting all products. ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* Delete all additional images of product by product id */
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "Delete additional images of a product",
            description = "Deletes all additional images associated with a product by product ID. Only the seller who owns the product can perform this operation.")
    @DeleteMapping("/delete-additional-images/{productId}")
    public ResponseEntity<?> deleteAdditionalImages(@PathVariable String productId) {
        if (productService.deleteAllAdditionalImagesByProductId(ObjectIdUtils.toObjectId(productId))) {
            return new ResponseEntity<>("Images deleted ", HttpStatus.OK);
        }
        return new ResponseEntity<>("No additional images found for this product.", HttpStatus.NOT_FOUND);
    }

}

