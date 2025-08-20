package br.com.redemaisfarma.adapters.outbound.auth.jwt.store;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.RefreshToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, RefreshToken> byToken = new ConcurrentHashMap<>();

    @Override
    public RefreshToken save(RefreshToken rt) {
        if (rt.getId() == null) {
            rt.setId(UUID.randomUUID());
        }
        byToken.put(rt.getToken(), rt);
        return rt;
    }

    // >>> assinatura correta exigida pela interface
    @Override
    public Optional<RefreshToken> findValidByToken(String token) {
        RefreshToken found = byToken.get(token);
        if (found == null)
            return Optional.empty();

        Instant now = Instant.now();
        if (found.isRevoked() || found.isExpired(now))
            return Optional.empty();

        return Optional.of(found);
    }

    @Override
    public void revokeByToken(String token, Instant revokedAt) {
        RefreshToken found = byToken.get(token);
        if (found != null && !found.isRevoked()) {
            found.setRevokedAt(revokedAt != null ? revokedAt : Instant.now());
        }
    }

    @Override
    public void revokeAllForUser(Long userId, String tenantId, Instant revokedAt) {
        Instant when = revokedAt != null ? revokedAt : Instant.now();
        byToken.values().stream().filter(rt -> rt.getUserId().equals(userId)
                && (tenantId == null || tenantId.equals(rt.getTenantId())) && !rt.isRevoked())
                .forEach(rt -> rt.setRevokedAt(when));
    }

    @Override
    public RefreshToken rotate(String oldToken, RefreshToken newToken, Instant revokedAt) {
        revokeByToken(oldToken, revokedAt);
        return save(newToken);
    }

    @Override
    public long deleteExpired(Instant now) {
        Instant ref = (now != null ? now : Instant.now());
        final long[] removed = { 0 };
        byToken.values().removeIf(rt -> {
            boolean exp = rt.isExpired(ref);
            if (exp)
                removed[0]++;
            return exp;
        });
        return removed[0];
    }
}
