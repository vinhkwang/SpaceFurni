package com.spacefurni.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalDiskProductImageStorageTest {

    @TempDir
    private Path storageDirectory;

    private LocalDiskProductImageStorage storage(Path directory) {
        return new LocalDiskProductImageStorage(directory.toString(), "http://localhost:8080");
    }

    @Test
    void storesFileOnDiskAndReturnsItsPublicUrl() throws IOException {
        LocalDiskProductImageStorage storage = storage(storageDirectory);
        MockMultipartFile file =
                new MockMultipartFile("file", "chair.jpg", "image/jpeg", "image-bytes".getBytes());

        String publicUrl = storage.store(file);

        assertThat(publicUrl).startsWith("http://localhost:8080/uploads/products/").endsWith(".jpg");
        String storedFileName = publicUrl.substring(publicUrl.lastIndexOf('/') + 1);
        assertThat(Files.readString(storageDirectory.resolve(storedFileName))).isEqualTo("image-bytes");
    }

    @Test
    void createsTheStorageDirectoryWhenItDoesNotExistYet() {
        Path nestedDirectory = storageDirectory.resolve("does-not-exist-yet");
        LocalDiskProductImageStorage storage = storage(nestedDirectory);
        MockMultipartFile file = new MockMultipartFile("file", "chair.png", "image/png", "image-bytes".getBytes());

        storage.store(file);

        assertThat(Files.isDirectory(nestedDirectory)).isTrue();
    }

    @Test
    void generatesAUniqueFileNameForEachUpload() {
        LocalDiskProductImageStorage storage = storage(storageDirectory);
        MockMultipartFile file = new MockMultipartFile("file", "chair.jpg", "image/jpeg", "image-bytes".getBytes());

        String firstUrl = storage.store(file);
        String secondUrl = storage.store(file);

        assertThat(firstUrl).isNotEqualTo(secondUrl);
    }
}
