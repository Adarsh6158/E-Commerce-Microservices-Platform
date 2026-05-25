package com.ecommerce.catalog_service.Exception;

public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException(String productId, String userId) {
        super("User " + userId + " has already reviewed product " + productId);
    }
}
