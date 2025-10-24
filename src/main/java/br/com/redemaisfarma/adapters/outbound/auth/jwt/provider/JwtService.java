/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.auth.jwt.provider;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenGenerationException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenValidationException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.JwtPrincipal;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.RefreshToken;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenPair;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.provider.TokenProvider;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklist;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class JwtService {
    private final TokenProvider tokenProvider;
    private final JwtProperties props;
    private final RefreshTokenStore refreshStore;
    private final TokenBlacklist blacklist;

    public JwtService(TokenProvider tokenProvider, JwtProperties props, RefreshTokenStore refreshStore, TokenBlacklist blacklist) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider);
        this.props = Objects.requireNonNull(props);
        this.refreshStore = Objects.requireNonNull(refreshStore);
        this.blacklist = Objects.requireNonNull(blacklist);
    }

    public TokenPair issueTokens(Long userId, String username, Collection<String> roles, String tenantId, Map<String, Object> extras) {
        try {
            Instant now = Instant.now();
            Instant accessExp = now.plus((long)this.props.getAccessTokenExpirationMinutes().intValue(), ChronoUnit.MINUTES);
            Instant refreshExp = now.plus((long)this.props.getRefreshTokenExpirationMinutes().intValue(), ChronoUnit.MINUTES);
            String jti = UUID.randomUUID().toString();
            HashMap<String, Object> claims = new HashMap<String, Object>();
            claims.put("uid", userId);
            claims.put("tenant", tenantId);
            claims.put("roles", roles == null ? List.of() : List.copyOf(roles));
            if (extras != null && !extras.isEmpty()) {
                claims.put("meta", new HashMap<String, Object>(extras));
            }
            String access = this.tokenProvider.generateAccessToken(username, claims, accessExp);
            HashMap<String, Object> refreshClaims = new HashMap<String, Object>(claims);
            refreshClaims.put("typ", "refresh");
            refreshClaims.put("jti", jti);
            String refresh = this.tokenProvider.generateRefreshToken(username, jti, refreshClaims, refreshExp);
            RefreshToken rt = RefreshToken.newToken(userId, tenantId, refresh, now, refreshExp, extras != null ? (String)extras.get("userAgent") : null, extras != null ? (String)extras.get("ip") : null);
            this.refreshStore.save(rt);
            return new TokenPair(access, refresh, now, accessExp);
        }
        catch (RuntimeException ex) {
            throw new TokenGenerationException("Erro ao emitir tokens", ex);
        }
    }

    public JwtPrincipal validateAccessToken(String rawToken) {
        String token = this.stripPrefix(rawToken);
        if (this.blacklist.isBlacklisted(token)) {
            throw new TokenValidationException("Token revogado");
        }
        JwtPrincipal principal = this.tokenProvider.validate(token);
        if (this.props.getIssuer() != null && !this.props.getIssuer().equals(principal.getIssuer())) {
            throw new TokenValidationException("Issuer inv\u00e1lido");
        }
        if (this.props.getAudience() != null && principal.getAudience() != null && !principal.getAudience().contains(this.props.getAudience())) {
            throw new TokenValidationException("Audience inv\u00e1lida");
        }
        return principal;
    }

    public TokenPair rotateRefreshToken(String rawRefreshToken) {
        String token = this.stripPrefix(rawRefreshToken);
        if (this.blacklist.isBlacklisted(token)) {
            throw new TokenValidationException("Refresh token revogado");
        }
        JwtPrincipal principal = this.tokenProvider.validate(token);
        boolean valid = this.refreshStore.findValidByToken(token).isPresent();
        if (!valid) {
            throw new TokenValidationException("Refresh expirado/inv\u00e1lido");
        }
        this.refreshStore.revokeByToken(token, Instant.now());
        Instant exp = principal.getExpiresAt() != null ? principal.getExpiresAt() : Instant.now().plus(15L, ChronoUnit.MINUTES);
        this.blacklist.blacklist(token, exp);
        return this.issueTokens(principal.getUserId(), principal.getSubject(), principal.getRoles(), principal.getTenant(), Map.of("rotated", true));
    }

    public void revokeAccessToken(String rawAccessToken, Instant expiresAt) {
        String token = this.stripPrefix(rawAccessToken);
        this.blacklist.blacklist(token, expiresAt != null ? expiresAt : Instant.now().plus(1L, ChronoUnit.HOURS));
    }

    public void revokeAllRefreshForUser(Long userId, String tenantId) {
        this.refreshStore.revokeAllForUser(userId, tenantId, Instant.now());
    }

    private String stripPrefix(String token) {
        if (token == null) {
            return null;
        }
        String prefix = this.props.getPrefix() + " ";
        return token.startsWith(prefix) ? token.substring(prefix.length()) : token;
    }

    public Map<String, Object> parse(String string) {
        throw new Error("Unresolved compilation problem: \n\tThe constructor TypeReference<Map<String,Object>>(JwtService) is undefined\n");
    }
}

