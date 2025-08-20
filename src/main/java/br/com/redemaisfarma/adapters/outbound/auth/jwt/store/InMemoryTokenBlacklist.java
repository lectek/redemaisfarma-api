package br.com.redemaisfarma.adapters.outbound.auth.jwt.store;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blacklist in-memory (dev/test). Produção: prefira Redis.
 */
@Component
public class InMemoryTokenBlacklist implements TokenBlacklist {

    private final Map<String, Instant> map = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String token, Instant until) {
        if (token == null)
            return;
        map.put(token, until != null ? until : Instant.now().plusSeconds(3600));
    }

    @Override
    public boolean isBlacklisted(String token) {
        Instant until = map.get(token);
        if (until == null)
            return false;
        if (Instant.now().isAfter(until)) {
            map.remove(token);
            return false;
        }
        return true;
    }
}
