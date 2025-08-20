package br.com.redemaisfarma.adapters.outbound.auth.jwt.service;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenSubject;

import java.util.Map;

public interface JwtTokenService {

    /**
     * Gera um access token (JWT) a partir do subject e claims adicionais. Retorne APENAS o JWT (sem prefixo). Quem
     * quiser prefixo adiciona depois.
     */
    String generateAccessToken(TokenSubject subject, Map<String, Object> customClaims);

    /**
     * Valida o JWT (assinatura/expiração) e retorna os claims como Map. Lança exceções específicas em caso de erro
     * (ex.: TokenExpiredException, InvalidJwtTokenException).
     */
    Map<String, Object> parseAndValidate(String jwt);
}
