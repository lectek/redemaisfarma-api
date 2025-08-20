package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    long deleteByExpiresAtBefore(Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshTokenEntity r
               set r.revokedAt = :when
             where r.userId = :userId
               and r.revokedAt is null
               and (:tenantId is null or r.tenantId = :tenantId)
            """)
    int revokeAllForUser(@Param("userId") Long userId, @Param("tenantId") String tenantId, @Param("when") Instant when);
}
