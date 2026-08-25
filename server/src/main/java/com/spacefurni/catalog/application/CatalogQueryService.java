package com.spacefurni.catalog.application;

import com.spacefurni.catalog.api.dto.CategoryTreeResponse;
import com.spacefurni.catalog.api.dto.ProductDetailResponse;
import com.spacefurni.catalog.api.dto.ProductSummaryResponse;
import com.spacefurni.catalog.api.mapper.CategoryResponseMapper;
import com.spacefurni.catalog.api.mapper.ProductResponseMapper;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import com.spacefurni.catalog.infrastructure.ProductRepository;
import com.spacefurni.catalog.infrastructure.ProductSearchSpecifications;
import com.spacefurni.shared.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogQueryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductResponseMapper productResponseMapper;
    private final CategoryResponseMapper categoryResponseMapper;

    public CatalogQueryService(ProductRepository productRepository, CategoryRepository categoryRepository,
            ProductResponseMapper productResponseMapper, CategoryResponseMapper categoryResponseMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productResponseMapper = productResponseMapper;
        this.categoryResponseMapper = categoryResponseMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> findPublishedProducts(ProductFilter filter, Pageable pageable) {
        Specification<Product> specification = buildSpecification(filter);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                toSort(filter.sort()));
        return productRepository.findAll(specification, sortedPageable).map(productResponseMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse findProductDetailBySlug(String slug) {
        Product product = findPublishedProductBySlugOrThrow(slug);
        List<Product> relatedProducts = fetchRelatedProductEntities(product, 3);
        return productResponseMapper.toDetail(product, relatedProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> findRelatedProducts(String slug, int limit) {
        Product product = findPublishedProductBySlugOrThrow(slug);
        return fetchRelatedProductEntities(product, limit).stream().map(productResponseMapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> findCategoryTree() {
        List<Category> departments = categoryRepository.findAllByParentIsNullOrderByDisplayOrder();
        Map<UUID, Long> productCountsBySubCategoryId = new HashMap<>();
        for (Category department : departments) {
            for (Category subCategory : department.getChildren()) {
                long productCount = productRepository.count(ProductSearchSpecifications.publishedOnly()
                        .and(ProductSearchSpecifications.inSubCategory(subCategory.getSlug())));
                productCountsBySubCategoryId.put(subCategory.getId(), productCount);
            }
        }
        return departments.stream().map(department -> categoryResponseMapper.toTree(department,
                productCountsBySubCategoryId)).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> suggestProducts(String term, int limit) {
        Specification<Product> specification = ProductSearchSpecifications.publishedOnly()
                .and(ProductSearchSpecifications.nameOrCategoryContains(term))
                .and(ProductSearchSpecifications.withCategoryFetched());
        return productRepository.findAll(specification, PageRequest.of(0, limit)).stream()
                .map(productResponseMapper::toSummary).toList();
    }

    private Product findPublishedProductBySlugOrThrow(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + slug));
        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Product not found: " + slug);
        }
        return product;
    }

    private List<Product> fetchRelatedProductEntities(Product product, int limit) {
        Specification<Product> specification = ProductSearchSpecifications.publishedOnly()
                .and(ProductSearchSpecifications.inSubCategory(product.getCategory().getSlug()))
                .and(ProductSearchSpecifications.withCategoryFetched());
        return productRepository.findAll(specification, PageRequest.of(0, limit + 1)).stream()
                .filter(candidate -> !candidate.getId().equals(product.getId())).limit(limit).toList();
    }

    private Specification<Product> buildSpecification(ProductFilter filter) {
        Specification<Product> specification = ProductSearchSpecifications.publishedOnly()
                .and(ProductSearchSpecifications.withCategoryFetched());
        if (filter.departmentSlug() != null) {
            specification = specification.and(ProductSearchSpecifications.inDepartment(filter.departmentSlug()));
        }
        if (filter.subCategorySlug() != null) {
            specification = specification.and(ProductSearchSpecifications.inSubCategory(filter.subCategorySlug()));
        }
        if (filter.minPriceAmount() != null || filter.maxPriceAmount() != null) {
            specification = specification
                    .and(ProductSearchSpecifications.priceBetween(filter.minPriceAmount(), filter.maxPriceAmount()));
        }
        return specification;
    }

    private Sort toSort(ProductSortOption sortOption) {
        ProductSortOption resolvedSortOption = sortOption == null ? ProductSortOption.NEWEST : sortOption;
        return switch (resolvedSortOption) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price.amount");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price.amount");
            case RATING -> Sort.by(Sort.Direction.DESC, "ratingAverage");
        };
    }
}
