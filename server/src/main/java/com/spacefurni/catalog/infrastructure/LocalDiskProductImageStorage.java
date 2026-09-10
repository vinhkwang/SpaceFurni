package com.spacefurni.catalog.infrastructure;

import com.spacefurni.catalog.application.ProductImageStorage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalDiskProductImageStorage implements ProductImageStorage {

    private final Path storageDirectory;
    private final String publicBaseUrl;

    public LocalDiskProductImageStorage(
            @Value("${spacefurni.storage.product-images-directory}") String productImagesDirectory,
            @Value("${spacefurni.storage.public-base-url}") String publicBaseUrl) {
        this.storageDirectory = Paths.get(productImagesDirectory).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public String store(MultipartFile file) {
        Path storedFilePath = resolveStorageDirectory().resolve(generateStoredFileName(file));
        copyFileToDisk(file, storedFilePath);
        return buildPublicUrl(storedFilePath.getFileName().toString());
    }

    private Path resolveStorageDirectory() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException exception) {
            throw new ProductImageStorageException("Could not create product image storage directory", exception);
        }
        return storageDirectory;
    }

    private String generateStoredFileName(MultipartFile file) {
        return UUID.randomUUID() + fileExtension(file.getOriginalFilename());
    }

    private String fileExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : originalFilename.substring(lastDotIndex).toLowerCase();
    }

    private void copyFileToDisk(MultipartFile file, Path targetPath) {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ProductImageStorageException("Could not save uploaded product image", exception);
        }
    }

    private String buildPublicUrl(String storedFileName) {
        return publicBaseUrl + ProductImageUrls.URL_PREFIX + storedFileName;
    }
}
