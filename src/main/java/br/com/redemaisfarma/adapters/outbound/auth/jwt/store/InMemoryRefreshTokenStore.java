/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.outbound.auth.jwt.store;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.RefreshToken;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="jwt.refresh-store", name={"type"}, havingValue="memory")
public class InMemoryRefreshTokenStore
implements RefreshTokenStore {
    private final Clock clock;
    private final Map<String, Entry> byToken = new ConcurrentHashMap<String, Entry>();
    private final Map<Long, CopyOnWriteArraySet<String>> tokensByUser = new ConcurrentHashMap<Long, CopyOnWriteArraySet<String>>();

    public InMemoryRefreshTokenStore(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public Optional<RefreshToken> findValidByToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Entry e = this.byToken.get(token);
        if (e == null) {
            return Optional.empty();
        }
        Instant now = this.clock.instant();
        boolean expired = e.expiresAt != null && e.expiresAt.isBefore(now);
        boolean revoked = e.revokedAt != null;
        boolean bl = revoked;
        if (expired || revoked) {
            return Optional.empty();
        }
        return Optional.of(e.toModel());
    }

    @Override
    public RefreshToken rotate(String currentToken, RefreshToken newToken, Instant revokedAt) {
        if (currentToken != null) {
            this.internalRevoke(currentToken, revokedAt != null ? revokedAt : this.clock.instant());
        }
        return this.save(newToken);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        Objects.requireNonNull(token, "token");
        Entry e = Entry.fromModel(token);
        this.byToken.put(e.token, e);
        this.tokensByUser.computeIfAbsent(e.userId, k -> new CopyOnWriteArraySet()).add(e.token);
        return e.toModel();
    }

    @Override
    public void revokeAllForUser(Long l, String string, Instant instant) {
        throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from element type Object to String\n");
    }

    @Override
    public void revokeByToken(String token, Instant revokedAt) {
        if (token == null) {
            return;
        }
        this.internalRevoke(token, revokedAt != null ? revokedAt : this.clock.instant());
    }

    @Override
    public long deleteExpired(Instant now) {
        Instant ref = now != null ? now : this.clock.instant();
        long count = 0L;
        Iterator<Map.Entry<String, Entry>> it = this.byToken.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Entry> me = it.next();
            Entry e = me.getValue();
            if (e.expiresAt == null || !e.expiresAt.isBefore(ref)) continue;
            it.remove();
            this.removeFromUserIndex(e.userId, e.token);
            ++count;
        }
        return count;
    }

    private void internalRevoke(String token, Instant when) {
        Entry e = this.byToken.get(token);
        if (e != null) {
            e.revokedAt = when;
        }
    }

    private void removeFromUserIndex(Long userId, String token) {
        if (userId == null) {
            return;
        }
        Set set = this.tokensByUser.get(userId);
        if (set != null) {
            set.remove(token);
            if (set.isEmpty()) {
                this.tokensByUser.remove(userId, (CopyOnWriteArraySet)set);
            }
        }
    }

    private static final class Entry {
        String token;
        Long userId;
        String tenantId;
        Instant issuedAt;
        Instant expiresAt;
        Instant revokedAt;
        String userAgent;
        String ipAddress;

        private Entry() {
        }

        static Entry fromModel(RefreshToken rt) {
            Entry e = new Entry();
            e.token = rt.getToken();
            e.userId = rt.getUserId();
            e.tenantId = rt.getTenantId();
            e.issuedAt = rt.getIssuedAt();
            e.expiresAt = rt.getExpiresAt();
            e.revokedAt = rt.getRevokedAt();
            e.userAgent = rt.getUserAgent();
            e.ipAddress = rt.getIpAddress();
            return e;
        }

        RefreshToken toModel() {
            RefreshToken rt = new RefreshToken();
            rt.setToken(this.token);
            rt.setUserId(this.userId);
            rt.setTenantId(this.tenantId);
            rt.setIssuedAt(this.issuedAt);
            rt.setExpiresAt(this.expiresAt);
            rt.setRevokedAt(this.revokedAt);
            rt.setUserAgent(this.userAgent);
            rt.setIpAddress(this.ipAddress);
            return rt;
        }
    }
}

