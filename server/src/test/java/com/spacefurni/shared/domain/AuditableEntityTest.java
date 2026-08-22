package com.spacefurni.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.config.JpaAuditingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class AuditableEntityTest {

    @Autowired
    private AuditableEntityTestRepository repository;

    @Test
    void persistingEntityPopulatesCreatedAndUpdatedTimestamps() {
        AuditableEntityTestFixture saved = repository.save(new AuditableEntityTestFixture());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
