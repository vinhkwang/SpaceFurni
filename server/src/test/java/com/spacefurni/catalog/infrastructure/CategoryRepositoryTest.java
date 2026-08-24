package com.spacefurni.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findsRootCategoriesOrderedByDisplayOrderAndExcludesChildren() {
        Category livingRoom = categoryRepository.save(new Category(null, "Living room", "living-room", null, 2));
        Category kitchen = categoryRepository.save(new Category(null, "Kitchen", "kitchen", null, 1));
        categoryRepository.save(new Category(livingRoom, "Sofa", "sofa", null, 1));

        List<Category> roots = categoryRepository.findAllByParentIsNullOrderByDisplayOrder();

        assertThat(roots).extracting(Category::getSlug).containsExactly("kitchen", "living-room");
    }

    @Test
    void findsCategoryBySlug() {
        categoryRepository.save(new Category(null, "Bedroom", "bedroom", null, 1));

        assertThat(categoryRepository.findBySlug("bedroom")).isPresent();
        assertThat(categoryRepository.findBySlug("bedroom").get().getName()).isEqualTo("Bedroom");
    }
}
