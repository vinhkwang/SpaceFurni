package com.spacefurni.catalog.application;

import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageUploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private final ProductImageStorage productImageStorage;

    public ProductImageUploadService(ProductImageStorage productImageStorage) {
        this.productImageStorage = productImageStorage;
    }

    public String uploadProductImage(MultipartFile file) {
        validateProductImageFile(file);
        return productImageStorage.store(file);
    }

    private void validateProductImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidProductImageException("An image file is required");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidProductImageException("Image must be JPEG, PNG or WebP");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidProductImageException("Image must be 5MB or smaller");
        }
    }
}
