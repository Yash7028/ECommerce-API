package com.ecommerce.ecomapi.service;

import com.ecommerce.ecomapi.entity.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    public String saveProduct(Product product);
    public boolean updateProduct(ObjectId id, Product product);
    public boolean deleteProductById(ObjectId id);
    public boolean deleteProductBySellerId(String sellerId);
    public boolean deleteAllProduct();
    public boolean deleteAllAdditionalImagesByProductId(ObjectId id) ;
    public Page<Product> getAll(int page, int size, String sortBy, String order);
    public Product getProductById(ObjectId id);
    public Page<Product> getProductsByCategory(String category,int page, int size, String sortBy, String order);
    public Page<Product> getProductsByCategoryAndSubCategory(String category, String subCategory,int page, int size, String sortBy, String order);
    public Page<Product> getProductsByProductName(String productName,int page, int size, String sortBy, String order);
    public Page<Product> searchProductsByName(String name,int page, int size, String sortBy, String order);
    public Page<Product> findProductsByExactTags(List<String> inputTags, int page, int size, String sortBy, String order);
    public Page<Product> findProductsByAnyTags(List<String> tags, int page, int size,String sortBy, String order);
    public Page<Product> getAllBySellerId( int page, int size, String sortBy, String direction);

}
