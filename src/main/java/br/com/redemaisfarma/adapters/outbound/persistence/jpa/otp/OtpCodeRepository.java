// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/jpa/otp/OtpCodeRepository.java
package br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCodeEntity, Long> {

    Optional<OtpCodeEntity> findByDeliveryId(String deliveryId);

    Optional<OtpCodeEntity> findFirstByDestinationOrderByCreatedAtDesc(String destination);

    long deleteByExpiresAtBefore(Instant now);

    Optional<OtpCodeEntity> findByVerificationToken(String verificationToken);
}
