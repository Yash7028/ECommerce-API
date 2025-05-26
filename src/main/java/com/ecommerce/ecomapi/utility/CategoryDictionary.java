package com.ecommerce.ecomapi.utility;

import java.util.List;
import java.util.Map;

public class CategoryDictionary {
    public static final Map<String, List<String>> CATEGORY_DICTIONARY = Map.of(
            "Electronics", List.of("Mobiles", "Laptops", "Tablets"),
            "Clothing", List.of("Men", "Women", "Kids"),
            "Books", List.of("Fiction", "Education", "Comics")
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
