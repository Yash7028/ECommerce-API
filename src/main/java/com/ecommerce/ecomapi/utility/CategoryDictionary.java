package com.ecommerce.ecomapi.utility;

import java.util.List;
import java.util.Map;

public class CategoryDictionary {
    public static final Map<String, List<String>> CATEGORY_DICTIONARY = Map.of(
            "Electronics", List.of("Mobiles", "Laptops", "Tablets", "Cameras", "Accessories"),
            "Clothing", List.of("Men", "Women", "Kids", "Winter Wear", "Ethnic Wear"),
            "Books", List.of("Fiction", "Non-fiction", "Education", "Comics", "Biographies"),
            "Home & Kitchen", List.of("Furniture", "Home Decor", "Cookware", "Storage", "Lighting"),
            "Beauty & Health", List.of("Makeup", "Skincare", "Haircare", "Health Devices", "Nutrition"),
            "Sports & Outdoors", List.of("Fitness Equipment", "Sportswear", "Camping", "Cycling", "Footwear"),
            "Toys & Games", List.of("Educational Toys", "Board Games", "Outdoor Play", "Soft Toys", "Puzzles"),
            "Automotive", List.of("Car Accessories", "Bike Accessories", "Oils & Lubricants", "Spare Parts", "Car Electronics"),
            "Grocery", List.of("Beverages", "Snacks", "Dairy", "Vegetables", "Staples"),
            "Jewellery", List.of("Necklaces", "Rings", "Earrings", "Bracelets", "Watches")
    );

    // Optional helper method
    public static boolean isValidCategory(String category) {
        return CATEGORY_DICTIONARY.containsKey(category);
    }

    public static boolean isValidSubCategory(String category, String subCategory) {
        return CATEGORY_DICTIONARY.containsKey(category) &&
                CATEGORY_DICTIONARY.get(category).contains(subCategory);
    }
}
