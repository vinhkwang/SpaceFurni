package com.spacefurni.catalog.infrastructure;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProductImageWebConfiguration implements WebMvcConfigurer {

    private final String productImagesDirectory;

    public ProductImageWebConfiguration(
            @Value("${spacefurni.storage.product-images-directory}") String productImagesDirectory) {
        this.productImagesDirectory = productImagesDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path directory = Paths.get(productImagesDirectory).toAbsolutePath().normalize();
        registry.addResourceHandler(ProductImageUrls.URL_PATTERN).addResourceLocations("file:" + directory + "/");
    }
}
