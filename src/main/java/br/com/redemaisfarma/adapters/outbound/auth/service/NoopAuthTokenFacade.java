package br.com.redemaisfarma.adapters.outbound.auth.service;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenPair;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Primary
@ConditionalOnProperty(prefix = "jwt", name = "enabled", havingValue = "false", matchIfMissing = false)
public class NoopAuthTokenFacade implements AuthTokenOperations {

    private static RuntimeException disabled() {
        return new IllegalStateException("JWT está desabilitado neste profile");
    }

    @Override
    public TokenPair issueTokens(Long userId, String username, String tenantId, List<String> roles, String userAgent, String ip) {
        throw disabled();
    }

    @Override
    public TokenPair refreshUsing(String refreshTokenValue, String userAgent, String ip) {
        throw disabled();
    }

    @Override
    public void revokeRefreshToken(String refreshTokenValue) {
        // no-op
    }

    @Override
    public void revokeAllForUser(Long userId, String tenantId) {
        // no-op
    }

    @Override
    public Map<String, Object> validateAccessToken(String jwt) {
        throw disabled();
    }

    @Override
    public Map<String, Object> parseAccessToken(String jwt) {
        throw disabled();
    }

    @Override
    public void revokeAccessToken(String accessToken) {
        // no-op
    }
}
