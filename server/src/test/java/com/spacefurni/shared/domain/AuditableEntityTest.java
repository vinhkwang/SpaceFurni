package com.spacefurni.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.shared.config.JpaAuditingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import testfixtures.audit.AuditableEntityTestFixture;
import testfixtures.audit.AuditableEntityTestRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
@EntityScan(basePackageClasses = AuditableEntityTestFixture.class)
@EnableJpaRepositories(basePackageClasses = AuditableEntityTestRepository.class)
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
