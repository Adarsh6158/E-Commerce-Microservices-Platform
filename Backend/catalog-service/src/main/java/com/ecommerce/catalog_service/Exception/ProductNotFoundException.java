package com.ecommerce.catalog_service.Exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String identifier) {
        super("Product not found: " + identifier);
    }
}
