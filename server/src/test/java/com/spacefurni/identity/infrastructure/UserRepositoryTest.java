package com.spacefurni.identity.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.spacefurni.identity.domain.User;
import com.spacefurni.identity.domain.UserRole;
import com.spacefurni.shared.config.JpaAuditingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findsUserByEmailIgnoringCase() {
        userRepository.save(new User("Jane.Doe@Example.com", "hash", "Jane Doe", UserRole.CUSTOMER));

        assertThat(userRepository.findByEmailIgnoreCase("jane.doe@example.com")).isPresent();
    }

    @Test
    void reportsExistenceByEmailIgnoringCase() {
        userRepository.save(new User("John.Smith@Example.com", "hash", "John Smith", UserRole.CUSTOMER));

        assertThat(userRepository.existsByEmailIgnoreCase("john.smith@example.com")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("nobody@example.com")).isFalse();
    }
}
