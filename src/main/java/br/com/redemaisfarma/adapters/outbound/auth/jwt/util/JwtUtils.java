package br.com.redemaisfarma.adapters.outbound.auth.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Utilitário para operações comuns com JWT usando JJWT 0.12.x.
 */
public final class JwtUtils {

    private JwtUtils() {
    }

    public static String generateToken(Map<String, Object> claims, String subject, Instant expiration, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder().claims(claims == null ? Map.of() : claims).subject(subject).issuedAt(new Date())
                .expiration(Date.from(expiration)).signWith(key) // algoritmo (HS256) é inferido pela chave HMAC
                // .signWith(key, Jwts.SIG.HS256) // opcional, se quiser explicitar
                .compact();
    }

    public static Jws<Claims> parseToken(String token, String secret) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser().verifyWith(key) // substitui parserBuilder().setSigningKey(...)
                    .build().parseSignedClaims(token); // substitui parseClaimsJws(...)
        } catch (ExpiredJwtException e) {
            throw new br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenExpiredException(
                    "Token expirado.");
        } catch (JwtException e) {
            throw new br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.InvalidJwtTokenException(
                    "Token inválido.");
        }
    }
}
