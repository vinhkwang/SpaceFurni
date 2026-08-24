package com.spacefurni.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.domain.Product;
import com.spacefurni.catalog.domain.ProductStatus;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import com.spacefurni.shared.domain.Money;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class ProductRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void findBySlugFetchesCategoryAndChildCollectionsInOneStatement() {
        Category department = categoryRepository.save(new Category(null, "Living room", "living-room", null, 1));
        Category subCategory = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Product product = new Product("SKU-" + UUID.randomUUID(), "Oslo Sofa", "oslo-sofa-" + UUID.randomUUID(),
                subCategory, Money.ofVnd(5_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "200x90x80cm",
                "Fabric", "Beige", new BigDecimal("4.5"), 12, true, false);
        product.addImage("https://example.com/a.jpg", 1);
        product.addSpecification("Material", "Fabric", 1);
        product.addColorSwatch("#000000", 1);
        entityManager.persistAndFlush(product);
        entityManager.clear();

        Statistics statistics = statistics();
        statistics.clear();

        Optional<Product> found = productRepository.findBySlug(product.getSlug());

        assertThat(found).isPresent();
        assertThat(found.get().getCategory().getName()).isEqualTo("Sofa");
        assertThat(found.get().getImages()).hasSize(1);
        assertThat(found.get().getSpecifications()).hasSize(1);
        assertThat(found.get().getColorSwatches()).hasSize(1);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void composedSpecificationsFilterInOneStatementPerPage() {
        Category department = categoryRepository.save(new Category(null, "Kitchen", "kitchen", null, 1));
        Category subCategory = categoryRepository
                .save(new Category(department, "Table", "table-" + UUID.randomUUID(), null, 1));
        Product matching = new Product("SKU-" + UUID.randomUUID(), "Birch Table",
                "birch-table-" + UUID.randomUUID(), subCategory, Money.ofVnd(2_000_000L), null,
                ProductStatus.PUBLISHED, "short", "long", "120x60cm", "Birch", "Natural", new BigDecimal("4.0"), 3,
                false, false);
        Product tooExpensive = new Product("SKU-" + UUID.randomUUID(), "Marble Table",
                "marble-table-" + UUID.randomUUID(), subCategory, Money.ofVnd(20_000_000L), null,
                ProductStatus.PUBLISHED, "short", "long", "120x60cm", "Marble", "White", new BigDecimal("4.0"), 3,
                false, false);
        Product draft = new Product("SKU-" + UUID.randomUUID(), "Draft Table", "draft-table-" + UUID.randomUUID(),
                subCategory, Money.ofVnd(2_000_000L), null, ProductStatus.DRAFT, "short", "long", "120x60cm", "Pine",
                "Natural", new BigDecimal("4.0"), 3, false, false);
        entityManager.persistAndFlush(matching);
        entityManager.persistAndFlush(tooExpensive);
        entityManager.persistAndFlush(draft);
        entityManager.clear();

        Specification<Product> specification = ProductSearchSpecifications.publishedOnly()
                .and(ProductSearchSpecifications.inSubCategory(subCategory.getSlug()))
                .and(ProductSearchSpecifications.priceBetween(1_000_000L, 5_000_000L));

        Statistics statistics = statistics();
        statistics.clear();

        Page<Product> page = productRepository.findAll(specification, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Product::getName).containsExactly("Birch Table");
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void inDepartmentFiltersByCategoryParentSlug() {
        Category livingRoom = categoryRepository.save(new Category(null, "Living room", "living-room", null, 1));
        Category kitchen = categoryRepository.save(new Category(null, "Kitchen", "kitchen", null, 2));
        Category sofa = categoryRepository
                .save(new Category(livingRoom, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Category table = categoryRepository
                .save(new Category(kitchen, "Table", "table-" + UUID.randomUUID(), null, 1));
        Product sofaProduct = new Product("SKU-" + UUID.randomUUID(), "Oslo Sofa", "oslo-sofa-" + UUID.randomUUID(),
                sofa, Money.ofVnd(5_000_000L), null, ProductStatus.PUBLISHED, "short", "long", "200x90x80cm",
                "Fabric", "Beige", new BigDecimal("4.5"), 12, true, false);
        Product tableProduct = new Product("SKU-" + UUID.randomUUID(), "Oak Table",
                "oak-table-" + UUID.randomUUID(), table, Money.ofVnd(3_000_000L), null, ProductStatus.PUBLISHED,
                "short", "long", "120x60cm", "Oak", "Natural", new BigDecimal("4.0"), 3, false, false);
        entityManager.persistAndFlush(sofaProduct);
        entityManager.persistAndFlush(tableProduct);
        entityManager.clear();

        List<Product> livingRoomProducts = productRepository.findAll(
                ProductSearchSpecifications.inDepartment("living-room"));

        assertThat(livingRoomProducts).extracting(Product::getName).containsExactly("Oslo Sofa");
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}
