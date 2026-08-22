package testfixtures.audit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditableEntityTestRepository extends JpaRepository<AuditableEntityTestFixture, UUID> {
}
