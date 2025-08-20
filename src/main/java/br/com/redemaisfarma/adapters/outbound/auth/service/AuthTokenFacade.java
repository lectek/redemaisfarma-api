package br.com.redemaisfarma.adapters.outbound.auth.service;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.RefreshToken;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenPair;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenSubject;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.service.JwtTokenService;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklistRepository;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AuthTokenFacade {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenBlacklistRepository blacklistRepository;
    private final JwtProperties props;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenFacade(JwtTokenService jwtTokenService, RefreshTokenStore refreshTokenStore,
            TokenBlacklistRepository blacklistRepository, JwtProperties props, Clock clock) {
        this.jwtTokenService = Objects.requireNonNull(jwtTokenService);
        this.refreshTokenStore = Objects.requireNonNull(refreshTokenStore);
        this.blacklistRepository = Objects.requireNonNull(blacklistRepository);
        this.props = Objects.requireNonNull(props);
        this.clock = Objects.requireNonNull(clock);
    }

    /** Emite um par (access + refresh) e persiste o refresh. */
    public TokenPair issueTokens(Long userId, String username, String tenantId, List<String> roles, String userAgent,
            String ip) {

        final Instant now = clock.instant();
        final Instant accessExp = now.plus(props.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
        final Instant refreshExp = now.plus(props.getRefreshTokenExpirationMinutes(), ChronoUnit.MINUTES);

        // ----- monta subject + claims e usa a assinatura nova do JwtTokenService
        TokenSubject subject = new TokenSubject(userId, username, tenantId, roles);
        Map<String, Object> claims = Map.of("tenantId", tenantId, "roles", roles == null ? List.of() : roles,
                "username", username == null ? "" : username, "typ", "access");

        String accessToken = jwtTokenService.generateAccessToken(subject, claims);

        // ----- refresh opaco armazenado no store
        String refreshOpaque = generateOpaqueToken();

        RefreshToken refresh = new RefreshToken(UUID.randomUUID(), userId, tenantId, refreshOpaque, now, refreshExp,
                null, userAgent, ip);
        refreshTokenStore.save(refresh);

        return new TokenPair(accessToken, refreshOpaque, now, accessExp);
    }

    /** Gera novo par a partir de um refresh token válido. Revoga o antigo. */
    public TokenPair refreshUsing(String refreshTokenValue, String userAgent, String ip) {
        final Instant now = clock.instant();

        if (blacklistRepository.exists(refreshTokenValue)) {
            throw new IllegalStateException("Refresh token em blacklist");
        }

        RefreshToken stored = refreshTokenStore.findValidByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalStateException("Refresh token não encontrado/expirado/revogado"));

        // revoga o antigo e coloca na blacklist até expirar
        refreshTokenStore.revokeByToken(refreshTokenValue, now);
        blacklistRepository.save(refreshTokenValue, stored.getExpiresAt());

        return issueTokens(stored.getUserId(), null, stored.getTenantId(), List.of(), userAgent, ip);
    }

    public void revokeRefreshToken(String refreshTokenValue) {
        RefreshToken stored = refreshTokenStore.findValidByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalStateException("Refresh token não encontrado"));
        Instant now = clock.instant();
        refreshTokenStore.revokeByToken(refreshTokenValue, now);
        blacklistRepository.save(refreshTokenValue, stored.getExpiresAt());
    }

    public void revokeAllForUser(Long userId, String tenantId) {
        refreshTokenStore.revokeAllForUser(userId, tenantId, clock.instant());
    }

    public Map<String, Object> validateAccessToken(String jwt) {
        if (jwt == null || jwt.isBlank()) {
            throw new IllegalArgumentException("access token vazio");
        }
        // se estiver em blacklist, invalida
        String raw = stripPrefix(jwt, props.getPrefix());
        if (blacklistRepository.exists(raw)) {
            throw new IllegalStateException("Access token em blacklist");
        }
        return jwtTokenService.parseAndValidate(jwt);
    }

    public void revokeAccessToken(String accessToken) {
        Map<String, Object> claims = jwtTokenService.parseAndValidate(accessToken);
        Object expClaim = claims.get("exp");
        Instant exp = (expClaim instanceof Number) ? Instant.ofEpochSecond(((Number) expClaim).longValue())
                : clock.instant().plus(props.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        blacklistRepository.save(stripPrefix(accessToken, props.getPrefix()), exp);
    }

    // ----------------- helpers -----------------

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String stripPrefix(String token, String prefix) {
        if (token == null || prefix == null || prefix.isBlank())
            return token;
        String p = prefix + " ";
        return token.startsWith(p) ? token.substring(p.length()) : token;
    }
}
