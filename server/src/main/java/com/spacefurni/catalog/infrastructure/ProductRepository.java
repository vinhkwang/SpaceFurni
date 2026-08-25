package com.spacefurni.catalog.infrastructure;

import com.spacefurni.catalog.domain.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = { "category", "images", "specifications", "colorSwatches" })
    Optional<Product> findBySlug(String slug);
}
