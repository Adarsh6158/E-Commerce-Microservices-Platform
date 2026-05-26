package com.ecommerce.catalog_service.Validator;

import com.ecommerce.catalog_service.Dto.Request.CreateProductRequest;
import com.ecommerce.catalog_service.Dto.Request.UpdateProductRequest;
import com.ecommerce.catalog_service.Exception.InvalidRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductValidator {

    public void validateCreateRequest(CreateProductRequest request) {
        if (request.basePrice() != null && request.basePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("Base price must not be negative");
        }
        if (request.sku() != null && request.sku().length() > 50) {
            throw new InvalidRequestException("SKU must not exceed 50 characters");
        }
    }

    public void validateUpdateRequest(UpdateProductRequest request) {
        if (request.basePrice() != null && request.basePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("Base price must not be negative");
        }
    }
}
