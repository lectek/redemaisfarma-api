package br.com.redemaisfarma.adapters.outbound.auth.jwt.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class RefreshToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long userId;
    private String tenantId;
    private String token; // opaque
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant revokedAt; // null se válido
    private String userAgent; // opcional
    private String ipAddress; // opcional

    public RefreshToken() {
    }

    public RefreshToken(UUID id, Long userId, String tenantId, String token, Instant issuedAt, Instant expiresAt,
            Instant revokedAt, String userAgent, String ipAddress) {
        this.id = id;
        this.userId = userId;
        this.tenantId = tenantId;
        this.token = token;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    /** Factory recomendado p/ criação de um novo RT */
    public static RefreshToken newToken(Long userId, String tenantId, String token, Instant issuedAt, Instant expiresAt,
            String userAgent, String ipAddress) {
        return new RefreshToken(UUID.randomUUID(), userId, tenantId, token, issuedAt, expiresAt, null, userAgent,
                ipAddress);
    }

    /** Conveniências de domínio */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }

    public boolean isValid(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    public void revoke(Instant at) {
        this.revokedAt = at == null ? Instant.now() : at;
    }

    // getters/setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RefreshToken))
            return false;
        RefreshToken that = (RefreshToken) o;
        // id OU token já garantem identidade; mantenho os dois como você fez
        return Objects.equals(id, that.id) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, token);
    }

    @Override
    public String toString() {
        return "RefreshToken{id=" + id + ", userId=" + userId + ", tenantId='" + tenantId + '\''
                + ", token='***redacted***'" + ", issuedAt=" + issuedAt + ", expiresAt=" + expiresAt + ", revokedAt="
                + revokedAt + '}';
    }
}
