package com.spacefurni.catalog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.catalog.domain.Category;
import java.util.List;
import java.util.UUID;
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
        String livingRoomSlug = "living-room-" + UUID.randomUUID();
        String kitchenSlug = "kitchen-" + UUID.randomUUID();
        String sofaSlug = "sofa-" + UUID.randomUUID();
        Category livingRoom = categoryRepository.save(new Category(null, "Living room", livingRoomSlug, null, 2));
        categoryRepository.save(new Category(null, "Kitchen", kitchenSlug, null, 1));
        categoryRepository.save(new Category(livingRoom, "Sofa", sofaSlug, null, 1));

        List<Category> roots = categoryRepository.findAllByParentIsNullOrderByDisplayOrder();

        assertThat(roots).extracting(Category::getSlug).containsSubsequence(kitchenSlug, livingRoomSlug);
        assertThat(roots).extracting(Category::getSlug).doesNotContain(sofaSlug);
    }

    @Test
    void findsCategoryBySlug() {
        String bedroomSlug = "bedroom-" + UUID.randomUUID();
        categoryRepository.save(new Category(null, "Bedroom", bedroomSlug, null, 1));

        assertThat(categoryRepository.findBySlug(bedroomSlug)).isPresent();
        assertThat(categoryRepository.findBySlug(bedroomSlug).get().getName()).isEqualTo("Bedroom");
    }
}
