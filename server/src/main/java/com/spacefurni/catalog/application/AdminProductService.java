package com.spacefurni.catalog.application;

import com.spacefurni.catalog.api.dto.AdminProductRequest;
import com.spacefurni.catalog.api.dto.AdminProductRowResponse;
import com.spacefurni.catalog.api.dto.StockAdjustmentRequest;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductImage;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.catalog.infrastructure.ProductSearchSpecifications;
import com.spacefurni.inventory.application.InventoryService;
import com.spacefurni.shared.domain.Money;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import com.spacefurni.shared.util.SlugGenerator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;

    public AdminProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public UUID createProduct(AdminProductRequest request) {
        Category department = findDepartmentBySlugOrThrow(request.departmentSlug());
        Category category = resolveSubCategory(department, request.subCategorySlug());
        String slug = SlugGenerator.generateUniqueSlug(request.title(),
                candidateSlug -> productRepository.findBySlug(candidateSlug).isPresent());
        String sku = generateSkuForDepartment(department);
        ProductStatus status = request.status() == null ? ProductStatus.DRAFT : request.status();

        Product product = new Product(sku, request.title(), slug, category, Money.ofVnd(request.price()), null,
                status, request.shortDescription(), request.longDescription(), request.dimensions(),
                request.material(), request.primaryColorName(), null, 0, false, false);
        if (request.imageUrl() != null) {
            product.addImage(request.imageUrl(), 1);
        }
        Product savedProduct = productRepository.save(product);

        inventoryService.provisionInitialStock(savedProduct.getId(), request.stock());

        return savedProduct.getId();
    }

    @Transactional
    public void updateProduct(UUID productId, AdminProductRequest request) {
        Product product = findProductByIdOrThrow(productId);
        if (!product.getVersion().equals(request.version())) {
            throw new OptimisticLockingFailureException("Product was modified by another request: " + productId);
        }
        Category department = findDepartmentBySlugOrThrow(request.departmentSlug());
        Category category = resolveSubCategory(department, request.subCategorySlug());
        ProductStatus status = request.status() == null ? product.getStatus() : request.status();

        product.updateDetails(request.title(), category, Money.ofVnd(request.price()), status,
                request.shortDescription(), request.longDescription(), request.dimensions(), request.material(),
                request.primaryColorName());
        if (request.imageUrl() != null) {
            product.replacePrimaryImage(request.imageUrl());
        }

        productRepository.save(product);
    }

    @Transactional
    public void archiveProduct(UUID productId) {
        Product product = findProductByIdOrThrow(productId);
        product.archive();
        productRepository.save(product);
    }

    @Transactional
    public void adjustStock(UUID productId, StockAdjustmentRequest request) {
        findProductByIdOrThrow(productId);
        inventoryService.adjustQuantityOnHand(productId, request.delta());
    }

    @Transactional(readOnly = true)
    public Page<AdminProductRowResponse> listProducts(String searchTerm, Pageable pageable) {
        Specification<Product> specification = ProductSearchSpecifications.withCategoryFetched();
        if (searchTerm != null && !searchTerm.isBlank()) {
            specification = specification.and(ProductSearchSpecifications.nameOrSkuOrCategoryContains(searchTerm));
        }
        Page<Product> products = productRepository.findAll(specification, pageable);
        List<UUID> productIds = products.getContent().stream().map(Product::getId).toList();
        Map<UUID, Integer> stockByProductId = inventoryService.findAvailableQuantities(productIds);
        return products.map(product -> toRow(product, stockByProductId.getOrDefault(product.getId(), 0)));
    }

    private AdminProductRowResponse toRow(Product product, int stockOnHand) {
        Category category = product.getCategory();
        String categoryLabel = category.getParent().getName() + " · " + category.getName();
        Money price = product.getPrice();
        return new AdminProductRowResponse(product.getId(), primaryImageUrl(product), product.getName(),
                product.getSku(), categoryLabel, price.amount(), price.currencyCode(), stockOnHand,
                product.getStatus());
    }

    private String primaryImageUrl(Product product) {
        return product.getImages().stream().min(Comparator.comparing(ProductImage::getDisplayOrder))
                .map(ProductImage::getUrl).orElse(null);
    }

    private Product findProductByIdOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private Category findDepartmentBySlugOrThrow(String departmentSlug) {
        Category department = categoryRepository.findBySlug(departmentSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentSlug));
        if (department.getParent() != null) {
            throw new ResourceNotFoundException("Department not found: " + departmentSlug);
        }
        return department;
    }

    private Category resolveSubCategory(Category department, String subCategorySlug) {
        if (subCategorySlug != null) {
            return department.getChildren().stream().filter(child -> child.getSlug().equals(subCategorySlug))
                    .findFirst().orElseThrow(
                            () -> new ResourceNotFoundException("Sub-category not found: " + subCategorySlug));
        }
        return department.getChildren().stream().min(Comparator.comparing(Category::getDisplayOrder)).orElseThrow(
                () -> new ResourceNotFoundException("Department has no sub-category: " + department.getSlug()));
    }

    private String generateSkuForDepartment(Category department) {
        String departmentPrefix = department.getSlug().replace("-", "").toUpperCase(Locale.ROOT);
        departmentPrefix = departmentPrefix.substring(0, Math.min(3, departmentPrefix.length()));
        int nextSequence = productRepository.findTopBySkuStartingWithOrderBySkuDesc(departmentPrefix + "-")
                .map(topProduct -> parseSkuSequence(topProduct.getSku()) + 1).orElse(1);
        return "%s-%04d".formatted(departmentPrefix, nextSequence);
    }

    private int parseSkuSequence(String sku) {
        return Integer.parseInt(sku.substring(sku.lastIndexOf('-') + 1));
    }
}
