package com.spacefurni.shared.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AuditableEntityTestRepository extends JpaRepository<AuditableEntityTestFixture, UUID> {
}
