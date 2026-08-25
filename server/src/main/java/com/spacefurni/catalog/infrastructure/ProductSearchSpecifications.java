package com.spacefurni.catalog.infrastructure;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSearchSpecifications {

    private ProductSearchSpecifications() {
    }

    public static Specification<Product> publishedOnly() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), ProductStatus.PUBLISHED);
    }

    public static Specification<Product> withCategoryFetched() {
        return (root, query, criteriaBuilder) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("category", JoinType.LEFT);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Product> inDepartment(String departmentSlug) {
        return (root, query, criteriaBuilder) -> {
            Join<Product, Category> category = root.join("category");
            Join<Category, Category> department = category.join("parent");
            return criteriaBuilder.equal(department.get("slug"), departmentSlug);
        };
    }

    public static Specification<Product> inSubCategory(String subCategorySlug) {
        return (root, query, criteriaBuilder) -> {
            Join<Product, Category> category = root.join("category");
            return criteriaBuilder.equal(category.get("slug"), subCategorySlug);
        };
    }

    public static Specification<Product> priceBetween(Long minAmount, Long maxAmount) {
        return (root, query, criteriaBuilder) -> {
            Path<Long> priceAmount = root.get("price").get("amount");
            if (minAmount != null && maxAmount != null) {
                return criteriaBuilder.between(priceAmount, minAmount, maxAmount);
            }
            if (minAmount != null) {
                return criteriaBuilder.greaterThanOrEqualTo(priceAmount, minAmount);
            }
            if (maxAmount != null) {
                return criteriaBuilder.lessThanOrEqualTo(priceAmount, maxAmount);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Product> nameOrCategoryContains(String term) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + term.toLowerCase() + "%";
            Join<Product, Category> category = root.join("category");
            return criteriaBuilder.or(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(category.get("name")), pattern));
        };
    }
}
