package br.com.redemaisfarma.adapters.outbound.auth.jwt.store;

import java.time.Instant;

/**
 * Registra tokens revogados temporariamente (até exp). Pode ter uma impl. em Redis no futuro.
 */
public interface TokenBlacklist {
    void blacklist(String token, Instant until);

    boolean isBlacklisted(String token);
}
