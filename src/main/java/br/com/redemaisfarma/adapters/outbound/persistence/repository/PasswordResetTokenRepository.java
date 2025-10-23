// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/repository/PasswordResetTokenRepository.java
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByToken(String token);
    long deleteByUsadoIsTrueOrExpiraEmBefore(LocalDateTime data);
    long deleteByUsuarioId(Long usuarioId); // << novo
}
