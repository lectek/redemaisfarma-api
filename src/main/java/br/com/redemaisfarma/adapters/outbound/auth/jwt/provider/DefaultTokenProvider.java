/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.jsonwebtoken.security.Keys
 */
package br.com.redemaisfarma.adapters.outbound.auth.jwt.provider;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.JwtPrincipal;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.provider.TokenProvider;
import io.jsonwebtoken.security.Keys;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.crypto.SecretKey;

public class DefaultTokenProvider
implements TokenProvider {
    private final JwtProperties props;
    private final SecretKey key;

    public DefaultTokenProvider(JwtProperties props) {
        this.props = Objects.requireNonNull(props);
        this.key = Keys.hmacShaKeyFor((byte[])props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(String string, Map<String, Object> map, Instant instant) {
        throw new Error("Unresolved compilation problem: \n\tThe method add(String) in the type CollectionMutator<String,NestedCollection<String,JwtBuilder>> is not applicable for the arguments (Object)\n");
    }

    @Override
    public String generateRefreshToken(String string, String string2, Map<String, Object> map, Instant instant) {
        throw new Error("Unresolved compilation problem: \n\tThe method add(String) in the type CollectionMutator<String,NestedCollection<String,JwtBuilder>> is not applicable for the arguments (Object)\n");
    }

    @Override
    public JwtPrincipal validate(String string) {
        throw new Error("Unresolved compilation problems: \n\tType mismatch: cannot convert from List<Object> to ArrayList<String>\n\tType mismatch: cannot convert from List<String> to ArrayList<String>\n\tType mismatch: cannot convert from List<Object> to ArrayList<String>\n");
    }

    private JwtPrincipal buildPrincipal(Long userId, String subject, List<String> roles, String tenant, String issuer, List<String> audience, Instant issuedAt, Instant expiresAt) {
        try {
            Constructor c1 = JwtPrincipal.class.getDeclaredConstructor(Long.class, String.class, List.class, String.class, String.class, List.class, Instant.class, Instant.class);
            c1.setAccessible(true);
            return (JwtPrincipal)c1.newInstance(userId, subject, roles, tenant, issuer, audience, issuedAt, expiresAt);
        }
        catch (NoSuchMethodException c1) {
        }
        catch (Exception e) {
            throw new RuntimeException("Erro invocando construtor JwtPrincipal(8 args)", e);
        }
        try {
            Constructor c2 = JwtPrincipal.class.getDeclaredConstructor(String.class, Long.class, String.class, List.class, String.class, List.class, Instant.class, Instant.class);
            c2.setAccessible(true);
            return (JwtPrincipal)c2.newInstance(subject, userId, tenant, roles, issuer, audience, issuedAt, expiresAt);
        }
        catch (NoSuchMethodException c2) {
        }
        catch (Exception e) {
            throw new RuntimeException("Erro invocando construtor JwtPrincipal(alt ordem)", e);
        }
        try {
            Method builder = JwtPrincipal.class.getMethod("builder", new Class[0]);
            Object b = builder.invoke(null, new Object[0]);
            DefaultTokenProvider.callIfExists(b, "userId", Long.class, userId);
            DefaultTokenProvider.callIfExists(b, "subject", String.class, subject);
            DefaultTokenProvider.callIfExists(b, "roles", List.class, roles);
            DefaultTokenProvider.callIfExists(b, "tenant", String.class, tenant);
            DefaultTokenProvider.callIfExists(b, "issuer", String.class, issuer);
            DefaultTokenProvider.callIfExists(b, "audience", List.class, audience);
            DefaultTokenProvider.callIfExists(b, "issuedAt", Instant.class, issuedAt);
            DefaultTokenProvider.callIfExists(b, "expiresAt", Instant.class, expiresAt);
            Method build = b.getClass().getMethod("build", new Class[0]);
            return (JwtPrincipal)build.invoke(b, new Object[0]);
        }
        catch (NoSuchMethodException builder) {
        }
        catch (Exception e) {
            throw new RuntimeException("Erro usando JwtPrincipal.builder()", e);
        }
        throw new IllegalStateException("N\u00e3o encontrei um construtor/builder compat\u00edvel para JwtPrincipal.");
    }

    private static void callIfExists(Object target, String method, Class<?> type, Object arg) {
        try {
            Method m = target.getClass().getMethod(method, type);
            m.invoke(target, arg);
        }
        catch (NoSuchMethodException m) {
        }
        catch (Exception e) {
            throw new RuntimeException("Falha invocando builder." + method, e);
        }
    }
}

