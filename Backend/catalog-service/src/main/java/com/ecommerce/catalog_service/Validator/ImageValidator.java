package com.ecommerce.catalog_service.Validator;

import com.ecommerce.catalog_service.Constant.AppConstants;
import com.ecommerce.catalog_service.Exception.InvalidRequestException;
import org.springframework.stereotype.Component;

@Component
public class ImageValidator {

    
    public void validateFilename(String filename) {
        if (filename == null || !filename.matches(AppConstants.IMAGE_FILENAME_PATTERN)) {
            throw new InvalidRequestException("Invalid image filename: " + filename);
        }
    }

    
    public void validateContentType(String contentType, java.util.Collection<String> allowedTypes) {
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new InvalidRequestException("Unsupported file type: " + contentType);
        }
    }
}
