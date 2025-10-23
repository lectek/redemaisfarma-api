package br.com.redemaisfarma.adapters.outbound.auth.jwt.service;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.InvalidJwtTokenException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenExpiredException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenSubject;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class DefaultJwtTokenService implements JwtTokenService {

    private final JwtProperties props;
    private final SecretKey key;

    public DefaultJwtTokenService(JwtProperties props) {
        this.props = props;
        String secret = Objects.requireNonNull(props.getSecret(), "jwt.secret não pode ser nulo");
        if (secret.length() < 32) {
            throw new IllegalStateException("jwt.secret deve ter pelo menos 32 caracteres (HMAC).");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(TokenSubject subject, Map<String, Object> customClaims) {
        if (subject == null) throw new IllegalArgumentException("TokenSubject não pode ser nulo");

        String sub = resolveSubject(subject);
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(props.getAccessTokenExpirationMinutes()));

        Map<String, Object> claims = new HashMap<>();
        if (customClaims != null) claims.putAll(customClaims);
        claims.putIfAbsent("typ", "access");
        if (props.getAudience() != null && !props.getAudience().isBlank()) {
            claims.put("aud", props.getAudience());
        }

        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(sub)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(claims)
                .signWith(key)
                .compact();
    }

    @Override
    public Map<String, Object> parseAndValidate(String jwt) {
        try {
            Jwt<?, Claims> parsed = Jwts.parser()
                    .verifyWith(key)
                    .clockSkewSeconds(props.getClockSkewSeconds()) // << aqui é o método certo no 0.12.x
                    .build()
                    .parseSignedClaims(jwt);

            Claims claims = parsed.getPayload();

            String issuer = claims.getIssuer();
            if (issuer == null || !issuer.equals(props.getIssuer())) {
                throw new InvalidJwtTokenException("Issuer inválido");
            }

            if (props.getAudience() != null && !props.getAudience().isBlank()) {
                Object audClaim = claims.get("aud");
                if (audClaim == null) throw new InvalidJwtTokenException("Audience ausente");
                if (audClaim instanceof String s) {
                    if (!props.getAudience().equals(s)) throw new InvalidJwtTokenException("Audience inválida");
                } else if (audClaim instanceof Collection<?> col) {
                    if (!col.contains(props.getAudience())) throw new InvalidJwtTokenException("Audience inválida");
                }
            }

            return new HashMap<>(claims);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("JWT expirado", e);
        } catch (SignatureException e) {
            throw new InvalidJwtTokenException("Assinatura inválida", e);
        } catch (RuntimeException e) {
            throw new InvalidJwtTokenException("Token JWT inválido", e);
        }
    }

    private String resolveSubject(TokenSubject subject) {
        if (subject instanceof java.security.Principal p && p.getName() != null) return p.getName();
        String[] c = {"getSubject","subject","getUsername","username","getEmail","email","getId","id"};
        for (String m : c) try {
            Method mm = subject.getClass().getMethod(m);
            Object v = mm.invoke(subject);
            if (v != null) return String.valueOf(v);
        } catch (NoSuchMethodException ignored) {} catch (Exception ex) {}
        return subject.toString();
    }
}
