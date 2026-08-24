package com.spacefurni.catalog.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.api.dto.CategoryTreeResponse;
import com.spacefurni.catalog.domain.Category;
import com.spacefurni.catalog.infrastructure.CategoryRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryResponseMapperTest {

    private final CategoryResponseMapper mapper = new CategoryResponseMapper();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void toTreeRollsUpSubCategoryProductCountsIntoDepartmentCount() {
        Category department = categoryRepository
                .save(new Category(null, "Living room", "living-room-" + UUID.randomUUID(), null, 1));
        Category sofa = categoryRepository
                .save(new Category(department, "Sofa", "sofa-" + UUID.randomUUID(), null, 1));
        Category coffeeTable = categoryRepository
                .save(new Category(department, "Coffee table", "coffee-table-" + UUID.randomUUID(), null, 2));
        entityManager.flush();
        entityManager.clear();

        Category reloaded = categoryRepository.findById(department.getId()).orElseThrow();
        Map<UUID, Long> counts = Map.of(sofa.getId(), 3L, coffeeTable.getId(), 5L);

        CategoryTreeResponse tree = mapper.toTree(reloaded, counts);

        assertThat(tree.productCount()).isEqualTo(8L);
        assertThat(tree.subCategories()).hasSize(2);
        assertThat(tree.subCategories()).extracting(CategoryTreeResponse::productCount).containsExactly(3L, 5L);
    }

    @Test
    void toTreeUsesDirectCountForLeafCategory() {
        Category subCategory = categoryRepository
                .save(new Category(null, "Bookshelf", "bookshelf-" + UUID.randomUUID(), null, 1));
        entityManager.flush();
        entityManager.clear();

        Category reloaded = categoryRepository.findById(subCategory.getId()).orElseThrow();
        Map<UUID, Long> counts = Map.of(subCategory.getId(), 4L);

        CategoryTreeResponse tree = mapper.toTree(reloaded, counts);

        assertThat(tree.productCount()).isEqualTo(4L);
        assertThat(tree.subCategories()).isEmpty();
    }
}
