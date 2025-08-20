package br.com.redemaisfarma.adapters.outbound.auth.jwt.provider;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.InvalidJwtTokenException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenExpiredException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.JwtPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/**
 * Implementação JJWT (HS256) do TokenProvider.
 */
@Component
public class DefaultTokenProvider implements TokenProvider {

    private final JwtProperties props;
    private final SecretKey hmacKey;

    public DefaultTokenProvider(JwtProperties props) {
        this.props = props;
        this.hmacKey = Keys.hmacShaKeyFor(decodeSecret(props.getSecret()));
    }

    @Override
    public String generateAccessToken(String subject, Map<String, Object> claims, Instant expiresAt) {
        JwtBuilder b = Jwts.builder().claims(claims != null ? claims : Map.of()).subject(subject)
                .issuer(props.getIssuer()).issuedAt(Date.from(Instant.now())).expiration(Date.from(expiresAt))
                .signWith(hmacKey, Jwts.SIG.HS256);

        if (props.getAudience() != null && !props.getAudience().isBlank()) {
            b.audience().add(props.getAudience());
        }
        return b.compact();
    }

    @Override
    public String generateRefreshToken(String subject, String jti, Map<String, Object> claims, Instant expiresAt) {
        Map<String, Object> map = new HashMap<>();
        if (claims != null)
            map.putAll(claims);
        map.put("typ", "refresh");
        map.put("jti", jti);

        JwtBuilder b = Jwts.builder().claims(map).subject(subject).issuer(props.getIssuer())
                .issuedAt(Date.from(Instant.now())).expiration(Date.from(expiresAt)).signWith(hmacKey, Jwts.SIG.HS256);

        if (props.getAudience() != null && !props.getAudience().isBlank()) {
            b.audience().add(props.getAudience());
        }
        return b.compact();
    }

    @Override
    public JwtPrincipal validate(String token) {
        try {
            JwtParserBuilder parser = Jwts.parser().verifyWith(hmacKey).requireIssuer(props.getIssuer())
                    .clockSkewSeconds(props.getClockSkewSeconds());

            if (props.getAudience() != null && !props.getAudience().isBlank()) {
                parser.requireAudience(props.getAudience());
            }

            Jws<Claims> jws = parser.build().parseSignedClaims(token);
            Claims c = jws.getPayload();

            // Normalização para JwtPrincipal
            String subject = c.getSubject();
            String iss = c.getIssuer();
            List<String> aud = c.getAudience() != null ? List.copyOf(c.getAudience()) : null;
            String jti = c.getId() != null ? c.getId() : (String) c.get("jti");
            Instant exp = c.getExpiration() != null ? c.getExpiration().toInstant() : null;

            Long userId = null;
            Object uid = c.get("uid");
            if (uid instanceof Number)
                userId = ((Number) uid).longValue();
            else if (uid != null)
                try {
                    userId = Long.parseLong(uid.toString());
                } catch (NumberFormatException ignore) {
                }

            String tenant = c.get("tenant", String.class);

            List<String> roles = List.of();
            Object r = c.get("roles");
            if (r instanceof List<?>) {
                List<?> raw = (List<?>) r;
                List<String> out = new ArrayList<>(raw.size());
                for (Object it : raw)
                    out.add(String.valueOf(it));
                roles = List.copyOf(out);
            } else if (r != null) {
                roles = List.of(String.valueOf(r));
            }

            return new JwtPrincipal(subject, userId, tenant, roles, iss, aud, exp, jti, c);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token expirado.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtTokenException("Token inválido.", e);
        }
    }

    private static byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
