package br.com.redemaisfarma.adapters.outbound.auth.jwt.provider;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * Provedor de baixo nível para geração e validação de JWT. Usado pelo JwtService para operações de autenticação.
 */
public class JwtTokenProvider {

    public static final String CLAIM_SUBJECT = "sub";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_UID = "uid";

    private final JwtProperties props;
    private final SecretKey hmacKey;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        byte[] keyBytes = tryDecodeBase64(props.getSecret());
        this.hmacKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String subject, Map<String, Object> claims, Instant expiresAt) {
        return buildToken(claims, subject, expiresAt);
    }

    public String generateRefreshToken(String subject, String jti, Map<String, Object> claims, Instant expiresAt) {
        claims.put("jti", jti);
        claims.put("typ", "refresh");
        return buildToken(claims, subject, expiresAt);
    }

    /** Retorna o JWS com os claims já validados (sem usar tipo cru). */
    public Jws<Claims> parseAndValidate(String token) {
        return parse(token);
    }

    /** Caso prefira uma assinatura mais genérica: */
    public Jwt<JwsHeader, Claims> parseAndValidateAsJwt(String token) {
        return parse(token); // Jws<Claims> é um Jwt<JwsHeader, Claims>
    }

    /** Helper opcional: retorna apenas os claims. */
    public Claims parseClaims(String token) {
        return parse(token).getPayload();
    }

    private String buildToken(Map<String, Object> claims, String subject, Instant expiresAt) {
        Instant now = Instant.now();

        JwtBuilder builder = Jwts.builder().claims(claims).subject(subject).issuer(props.getIssuer())
                .issuedAt(Date.from(now)).notBefore(Date.from(now.minusSeconds(props.getClockSkewSeconds())))
                .expiration(Date.from(expiresAt)).signWith(hmacKey, Jwts.SIG.HS256);

        if (props.getAudience() != null && !props.getAudience().isBlank()) {
            builder.audience().add(props.getAudience());
        }

        return builder.compact();
    }

    private Jws<Claims> parse(String token) {
        JwtParserBuilder parser = Jwts.parser().verifyWith(hmacKey).requireIssuer(props.getIssuer())
                .clockSkewSeconds(props.getClockSkewSeconds());

        if (props.getAudience() != null && !props.getAudience().isBlank()) {
            parser.requireAudience(props.getAudience());
        }

        return parser.build().parseSignedClaims(token);
    }

    private static byte[] tryDecodeBase64(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            return secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
