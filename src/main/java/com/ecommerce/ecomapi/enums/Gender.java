package com.ecommerce.ecomapi.enums;

public enum Gender {
    MALE , FEMALE, OTHER;

    public static Gender fromStringIgnoreCase(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender value: " + value);
    }
}
