package com.spacefurni.catalog.application;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageStorage {

    String store(MultipartFile file);
}
