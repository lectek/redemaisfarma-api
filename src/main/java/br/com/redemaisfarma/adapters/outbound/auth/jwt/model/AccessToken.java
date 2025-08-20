package br.com.redemaisfarma.adapters.outbound.auth.jwt.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/** Value object para o access token gerado. */
public class AccessToken implements Serializable {
    private String token;
    private Instant expiresAt;

    public AccessToken() {
    }

    public AccessToken(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AccessToken))
            return false;
        AccessToken that = (AccessToken) o;
        return Objects.equals(token, that.token) && Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, expiresAt);
    }
}
