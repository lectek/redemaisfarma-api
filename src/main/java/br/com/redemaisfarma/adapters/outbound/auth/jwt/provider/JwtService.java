package br.com.redemaisfarma.adapters.outbound.auth.jwt.provider;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenGenerationException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenValidationException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.JwtPrincipal;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.RefreshToken;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenPair;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklist;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Serviço de alto nível para orquestrar operações com JWT.
 */
@Service
public class JwtService {

    private final TokenProvider tokenProvider;
    private final JwtProperties props;
    private final RefreshTokenStore refreshStore;
    private final TokenBlacklist blacklist;

    public JwtService(TokenProvider tokenProvider, JwtProperties props, RefreshTokenStore refreshStore,
            TokenBlacklist blacklist) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider);
        this.props = Objects.requireNonNull(props);
        this.refreshStore = Objects.requireNonNull(refreshStore);
        this.blacklist = Objects.requireNonNull(blacklist);
    }

    /**
     * Emite um par (access + refresh) para o usuário informado.
     */
    public TokenPair issueTokens(Long userId, String username, Collection<String> roles, String tenantId,
            Map<String, Object> extras) {
        try {
            final Instant now = Instant.now();
            final Instant accessExp = now.plus(props.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
            final Instant refreshExp = now.plus(props.getRefreshTokenExpirationMinutes(), ChronoUnit.MINUTES);
            final String jti = UUID.randomUUID().toString();

            // claims base
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", userId);
            claims.put("tenant", tenantId);
            claims.put("roles", roles == null ? List.of() : List.copyOf(roles));
            if (extras != null && !extras.isEmpty()) {
                claims.put("meta", new HashMap<>(extras));
            }

            // gera tokens
            String access = tokenProvider.generateAccessToken(username, claims, accessExp);

            Map<String, Object> refreshClaims = new HashMap<>(claims);
            refreshClaims.put("typ", "refresh");
            refreshClaims.put("jti", jti);
            String refresh = tokenProvider.generateRefreshToken(username, jti, refreshClaims, refreshExp);

            // persiste refresh (store trabalha por token, não por JTI)
            RefreshToken rt = RefreshToken.newToken(userId, tenantId, refresh, now, refreshExp,
                    extras != null ? (String) extras.get("userAgent") : null,
                    extras != null ? (String) extras.get("ip") : null);
            refreshStore.save(rt);

            // seu TokenPair tem 4 args: (access, refresh, issuedAt, expiresAt)
            return new TokenPair(access, refresh, now, accessExp);
        } catch (RuntimeException ex) {
            throw new TokenGenerationException("Erro ao emitir tokens", ex);
        }
    }

    /**
     * Valida um access token (aplica blacklist) e retorna o principal.
     */
    public JwtPrincipal validateAccessToken(String rawToken) {
        String token = stripPrefix(rawToken);
        if (blacklist.isBlacklisted(token)) {
            throw new TokenValidationException("Token revogado");
        }

        JwtPrincipal principal = tokenProvider.validate(token);

        if (props.getIssuer() != null && !props.getIssuer().equals(principal.getIssuer())) {
            throw new TokenValidationException("Issuer inválido");
        }
        if (props.getAudience() != null && principal.getAudience() != null
                && !principal.getAudience().contains(props.getAudience())) { // audience é LIST
            throw new TokenValidationException("Audience inválida");
        }

        return principal;
    }

    /**
     * Rotaciona um refresh token — invalida o antigo e emite novo par.
     */
    public TokenPair rotateRefreshToken(String rawRefreshToken) {
        String token = stripPrefix(rawRefreshToken);

        if (blacklist.isBlacklisted(token)) {
            throw new TokenValidationException("Refresh token revogado");
        }

        JwtPrincipal principal = tokenProvider.validate(token);

        // valida no store (a sua API verifica por token e data atual)
        boolean valid = refreshStore.findValidByToken(token).isPresent();
        if (!valid) {
            throw new TokenValidationException("Refresh expirado/inválido");
        }

        // invalida anterior e coloca na blacklist até expirar
        refreshStore.revokeByToken(token, Instant.now());
        Instant exp = principal.getExpiresAt() != null ? principal.getExpiresAt()
                : Instant.now().plus(15, ChronoUnit.MINUTES);
        blacklist.blacklist(token, exp);

        // emite novo par (marcando metadado de rotação)
        return issueTokens(principal.getUserId(), principal.getSubject(), principal.getRoles(), principal.getTenant(),
                Map.of("rotated", true));
    }

    /** Blacklist no access token atual. */
    public void revokeAccessToken(String rawAccessToken, Instant expiresAt) {
        String token = stripPrefix(rawAccessToken);
        blacklist.blacklist(token, expiresAt != null ? expiresAt : Instant.now().plus(1, ChronoUnit.HOURS));
    }

    /** Revoga todos os refresh de um usuário/tenant. */
    public void revokeAllRefreshForUser(Long userId, String tenantId) {
        refreshStore.revokeAllForUser(userId, tenantId, Instant.now());
    }

    private String stripPrefix(String token) {
        if (token == null)
            return null;
        String prefix = props.getPrefix() + " ";
        return token.startsWith(prefix) ? token.substring(prefix.length()) : token;
    }
}
