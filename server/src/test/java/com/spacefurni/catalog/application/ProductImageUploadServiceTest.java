package com.spacefurni.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ProductImageUploadServiceTest {

    private final ProductImageStorage productImageStorage = mock(ProductImageStorage.class);
    private final ProductImageUploadService productImageUploadService =
            new ProductImageUploadService(productImageStorage);

    @Test
    void delegatesAValidFileToStorageAndReturnsItsUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "chair.jpg", "image/jpeg", "image-bytes".getBytes());
        when(productImageStorage.store(file)).thenReturn("http://localhost:8080/uploads/products/chair.jpg");

        String imageUrl = productImageUploadService.uploadProductImage(file);

        assertThat(imageUrl).isEqualTo("http://localhost:8080/uploads/products/chair.jpg");
    }

    @Test
    void rejectsAnEmptyFileWithoutTouchingStorage() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "chair.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> productImageUploadService.uploadProductImage(emptyFile))
                .isInstanceOf(InvalidProductImageException.class);
    }

    @Test
    void rejectsAnUnsupportedContentType() {
        MockMultipartFile pdfFile =
                new MockMultipartFile("file", "manual.pdf", "application/pdf", "not-an-image".getBytes());

        assertThatThrownBy(() -> productImageUploadService.uploadProductImage(pdfFile))
                .isInstanceOf(InvalidProductImageException.class);
    }

    @Test
    void rejectsAFileLargerThanFiveMegabytes() {
        byte[] oversizedContent = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile oversizedFile = new MockMultipartFile("file", "chair.jpg", "image/jpeg", oversizedContent);

        assertThatThrownBy(() -> productImageUploadService.uploadProductImage(oversizedFile))
                .isInstanceOf(InvalidProductImageException.class);
    }

    @Test
    void neverCallsStorageForAnInvalidFile() {
        MockMultipartFile pdfFile =
                new MockMultipartFile("file", "manual.pdf", "application/pdf", "not-an-image".getBytes());

        assertThatThrownBy(() -> productImageUploadService.uploadProductImage(pdfFile))
                .isInstanceOf(InvalidProductImageException.class);

        verify(productImageStorage, never()).store(any(MultipartFile.class));
    }
}
