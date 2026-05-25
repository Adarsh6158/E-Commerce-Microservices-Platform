package com.ecommerce.catalog_service.Exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(String reviewId) {
        super("Review not found or not authorized: " + reviewId);
    }
}
