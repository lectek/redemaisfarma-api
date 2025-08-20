package br.com.redemaisfarma.adapters.outbound.auth.jwt.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Par de tokens de autenticação (access + refresh) e metadados. Use este VO no outbound. O mapeamento para DTOs da
 * camada application (ex.: AuthResponse) fica no application/service/controller.
 */
public class TokenPair implements Serializable {
    private final String accessToken;
    private final String refreshToken;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public TokenPair(String accessToken, String refreshToken, Instant issuedAt, Instant expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TokenPair))
            return false;
        TokenPair that = (TokenPair) o;
        return Objects.equals(accessToken, that.accessToken) && Objects.equals(refreshToken, that.refreshToken)
                && Objects.equals(issuedAt, that.issuedAt) && Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, issuedAt, expiresAt);
    }

    @Override
    public String toString() {
        return "TokenPair{accessToken='[PROTECTED]', refreshToken='[PROTECTED]', issuedAt=" + issuedAt + ", expiresAt="
                + expiresAt + '}';
    }
}
