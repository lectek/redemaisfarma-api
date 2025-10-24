/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.data.redis.core.StringRedisTemplate
 *  org.springframework.stereotype.Repository
 */
package br.com.redemaisfarma.adapters.outbound.cache.adapter;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklist;
import br.com.redemaisfarma.adapters.outbound.cache.config.RedisProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository(value="redisTokenBlacklist")
@ConditionalOnProperty(prefix="jwt.blacklist", name={"strategy"}, havingValue="redis")
public class RedisTokenBlacklistRepository
implements TokenBlacklist {
    private final StringRedisTemplate redis;
    private final RedisProperties props;

    public RedisTokenBlacklistRepository(StringRedisTemplate redis, RedisProperties props) {
        this.redis = Objects.requireNonNull(redis);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public boolean isBlacklisted(String string) {
        throw new Error("Unresolved compilation problem: \n\tThe method hasKey(String) in the type RedisTemplate<String,String> is not applicable for the arguments (Object)\n");
    }

    @Override
    public void blacklist(String string, Instant instant) {
        throw new Error("Unresolved compilation problems: \n\tThe method set(String, String, Duration) in the type ValueOperations<String,String> is not applicable for the arguments (Object, Object, Duration)\n\tThe method set(String, String, Duration) in the type ValueOperations<String,String> is not applicable for the arguments (Object, Object, Duration)\n");
    }

    private String buildKey(String raw) {
        return this.props.getKeyPrefix() + this.props.getBlacklistNamespace() + RedisTokenBlacklistRepository.sha256Url(raw);
    }

    private static String sha256Url(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        }
        catch (Exception e) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }
    }
}

