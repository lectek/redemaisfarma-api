package br.com.redemaisfarma.adapters.outbound.auth.jwt.provider;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.JwtPrincipal;

import java.time.Instant;
import java.util.Map;

/**
 * Abstração de baixo nível para assinar e validar JWT. O JwtService orquestrador usa esta interface.
 */
public interface TokenProvider {

    /**
     * Gera um Access Token com subject, claims adicionais e expiração.
     */
    String generateAccessToken(String subject, Map<String, Object> claims, Instant expiresAt);

    /**
     * Gera um Refresh Token com subject, JTI, claims e expiração.
     */
    String generateRefreshToken(String subject, String jti, Map<String, Object> claims, Instant expiresAt);

    /**
     * Valida o token (assinatura/claims) e retorna um principal normalizado. Deve lançar runtime exceptions específicas
     * em caso de erro.
     */
    JwtPrincipal validate(String token);
}
