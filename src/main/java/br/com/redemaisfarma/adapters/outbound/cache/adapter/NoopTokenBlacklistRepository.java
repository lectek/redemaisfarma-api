package br.com.redemaisfarma.adapters.outbound.cache.adapter;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklist;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * Implementação NO-OP. Só é carregada quando jwt.blacklist.strategy=noop.
 */
@Repository("noopTokenBlacklistRepository")
@ConditionalOnProperty(prefix = "jwt.blacklist", name = "strategy", havingValue = "noop")
public class NoopTokenBlacklistRepository implements TokenBlacklist {

    @Override
    public boolean isBlacklisted(String jti) {
        return false;
    }

    @Override
    public void blacklist(String jti, Instant expiresAt) {
        // no-op
    }

    @Override
    public void purgeExpired() {
        // no-op
    }
}
