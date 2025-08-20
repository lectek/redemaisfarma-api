package br.com.redemaisfarma.adapters.outbound.auth.jwt.store;

import java.time.Instant;

public interface TokenBlacklistRepository {

    /**
     * Adiciona um token ou JTI à blacklist.
     *
     * @param tokenOrJti
     *            token JWT ou identificador único
     * @param expiresAt
     *            instante em que o token expira (TTL)
     */
    void save(String tokenOrJti, Instant expiresAt);

    /**
     * Verifica se o token ou JTI está na blacklist.
     *
     * @param tokenOrJti
     *            token JWT ou identificador único
     *
     * @return true se estiver presente na blacklist
     */
    boolean exists(String tokenOrJti);
}
