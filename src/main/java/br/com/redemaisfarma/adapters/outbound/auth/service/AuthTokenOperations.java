package br.com.redemaisfarma.adapters.outbound.auth.service;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenPair;

import java.util.List;
import java.util.Map;

public interface AuthTokenOperations {

    TokenPair issueTokens(Long userId,
                          String username,
                          String tenantId,
                          List<String> roles,
                          String userAgent,
                          String ip);

    TokenPair refreshUsing(String refreshTokenValue, String userAgent, String ip);

    void revokeRefreshToken(String refreshTokenValue);

    void revokeAllForUser(Long userId, String tenantId);

    Map<String, Object> validateAccessToken(String jwt);

    Map<String, Object> parseAccessToken(String jwt);

    void revokeAccessToken(String accessToken);
}
