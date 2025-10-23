package br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Entidade JPA para códigos OTP.
 * Compatível com as migrations:
 *  - V20250913__create_otp_code.sql
 *  - V20250913_01__add_verification_token.sql
 *
 * status: PENDING | VERIFIED | EXPIRED | BLOCKED
 */
@Entity
@Table(
    name = "otp_code",
    indexes = {
        @Index(name = "idx_otp_destination", columnList = "destination"),
        @Index(name = "idx_otp_expires", columnList = "expires_at"),
        @Index(name = "idx_otp_verification_token", columnList = "verification_token")
    }
)
public class OtpCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador opaco desta tentativa/entrega (retornado no /start). */
    @Column(name = "delivery_id", length = 128, nullable = false, unique = true)
    private String deliveryId;

    /** Destino (e-mail ou telefone). */
    @Column(name = "destination", length = 255, nullable = false)
    private String destination;

    /** Hash do código (nunca armazene o código em claro). */
    @Column(name = "code_hash", length = 255, nullable = false)
    private String codeHash;

    /** Salt usado no hash do código. */
    @Column(name = "salt", length = 64, nullable = false)
    private String salt;

    /** TTL do código em segundos. */
    @Column(name = "ttl_seconds", nullable = false)
    private int ttlSeconds;

    /** Tentativas já realizadas. */
    @Column(name = "attempts", nullable = false)
    private int attempts;

    /** Máximo de tentativas permitidas. */
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    /** Data/hora de criação. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Data/hora de expiração. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Status do OTP: PENDING | VERIFIED | EXPIRED | BLOCKED
     * (Mantido como String para compatibilidade direta com a coluna VARCHAR.)
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** Token opaco one-time gerado no /verify (para /exchange). */
    @Column(name = "verification_token", length = 255)
    private String verificationToken;

    /** Momento em que o código foi validado com sucesso. */
    @Column(name = "verified_at")
    private Instant verifiedAt;

    /** Momento em que o verification_token foi consumido (exchange). */
    @Column(name = "consumed_at")
    private Instant consumedAt;

    /* ===========================
       Construtores
       =========================== */
    public OtpCodeEntity() {}

    /* ===========================
       Getters/Setters
       =========================== */
    public Long getId() { return id; }

    public String getDeliveryId() { return deliveryId; }
    public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public int getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(int ttlSeconds) { this.ttlSeconds = ttlSeconds; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }

    /* ===========================
       Helpers de estado (opcionais)
       =========================== */
    @Transient
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    @Transient
    public boolean isBlocked() {
        return "BLOCKED".equalsIgnoreCase(status);
    }

    /* ===========================
       equals/hashCode/toString
       =========================== */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OtpCodeEntity that)) return false;
        return Objects.equals(deliveryId, that.deliveryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryId);
    }

    @Override
    public String toString() {
        return "OtpCodeEntity{" +
                "id=" + id +
                ", deliveryId='" + deliveryId + '\'' +
                ", destination='" + destination + '\'' +
                ", attempts=" + attempts +
                ", status='" + status + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
