package br.com.redemaisfarma.adapters.outbound.auth.jwt.model;

import io.jsonwebtoken.Claims;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Principal normalizado extraído do JWT.
 */
public class JwtPrincipal implements Serializable {
    private final String subject;
    private final Long userId;
    private final String tenant;
    private final List<String> roles;
    private final String issuer;
    private final List<String> audience;
    private final Instant expiresAt;
    private final String jti;
    private final Claims rawClaims;

    public JwtPrincipal(String subject, Long userId, String tenant, List<String> roles, String issuer,
            List<String> audience, Instant expiresAt, String jti, Claims rawClaims) {
        this.subject = subject;
        this.userId = userId;
        this.tenant = tenant;
        this.roles = roles;
        this.issuer = issuer;
        this.audience = audience;
        this.expiresAt = expiresAt;
        this.jti = jti;
        this.rawClaims = rawClaims;
    }

    public String getSubject() {
        return subject;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTenant() {
        return tenant;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getIssuer() {
        return issuer;
    }

    public List<String> getAudience() {
        return audience;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getJti() {
        return jti;
    }

    public Claims getRawClaims() {
        return rawClaims;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof JwtPrincipal))
            return false;
        JwtPrincipal that = (JwtPrincipal) o;
        return Objects.equals(subject, that.subject) && Objects.equals(userId, that.userId)
                && Objects.equals(tenant, that.tenant) && Objects.equals(roles, that.roles)
                && Objects.equals(issuer, that.issuer) && Objects.equals(audience, that.audience)
                && Objects.equals(expiresAt, that.expiresAt) && Objects.equals(jti, that.jti);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, userId, tenant, roles, issuer, audience, expiresAt, jti);
    }
}
