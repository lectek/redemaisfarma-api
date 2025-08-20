package br.com.redemaisfarma.adapters.outbound.auth.jwt.store;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenStore {

    RefreshToken save(RefreshToken rt);

    Optional<RefreshToken> findValidByToken(String token);

    void revokeByToken(String token, Instant revokedAt);

    void revokeAllForUser(Long userId, String tenantId, Instant revokedAt);

    RefreshToken rotate(String oldToken, RefreshToken newToken, Instant revokedAt);

    /** Remove tokens expirados e retorna quantos foram apagados. */
    long deleteExpired(Instant now);
}
