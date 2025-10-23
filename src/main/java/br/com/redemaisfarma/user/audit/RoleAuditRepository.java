package br.com.redemaisfarma.user.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleAuditRepository extends JpaRepository<RoleAudit, Long> { }
