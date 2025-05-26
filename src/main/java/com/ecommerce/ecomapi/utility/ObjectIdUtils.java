package com.ecommerce.ecomapi.utility;

import org.bson.types.ObjectId;

public class ObjectIdUtils {
    public static String toString(ObjectId objectId) {
        return objectId != null ? objectId.toHexString() : null;
    }

    // Convert String to ObjectId
    public static ObjectId toObjectId(String id) {
        return (id != null && ObjectId.isValid(id)) ? new ObjectId(id) : null;
    }
}
