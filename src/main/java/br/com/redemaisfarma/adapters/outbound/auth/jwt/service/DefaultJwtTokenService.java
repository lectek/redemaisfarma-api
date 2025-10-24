/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.jsonwebtoken.Claims
 *  io.jsonwebtoken.ExpiredJwtException
 *  io.jsonwebtoken.Jws
 *  io.jsonwebtoken.Jwts
 *  io.jsonwebtoken.security.Keys
 *  io.jsonwebtoken.security.SignatureException
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.adapters.outbound.auth.jwt.service;

import br.com.redemaisfarma.adapters.outbound.auth.config.JwtProperties;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.InvalidJwtTokenException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.exception.TokenExpiredException;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.TokenSubject;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class DefaultJwtTokenService
implements JwtTokenService {
    private final JwtProperties props;
    private final SecretKey key;

    public DefaultJwtTokenService(JwtProperties props) {
        this.props = props;
        String secret = Objects.requireNonNull(props.getSecret(), "jwt.secret n\u00e3o pode ser nulo");
        if (secret.length() < 32) {
            throw new IllegalStateException("jwt.secret deve ter pelo menos 32 caracteres (HMAC).");
        }
        this.key = Keys.hmacShaKeyFor((byte[])secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(TokenSubject subject, Map<String, Object> customClaims) {
        if (subject == null) {
            throw new IllegalArgumentException("TokenSubject n\u00e3o pode ser nulo");
        }
        String sub = this.resolveSubject(subject);
        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(this.props.getAccessTokenExpirationMinutes().intValue()));
        HashMap<String, Object> claims = new HashMap<String, Object>();
        if (customClaims != null) {
            claims.putAll(customClaims);
        }
        claims.putIfAbsent("typ", "access");
        if (this.props.getAudience() != null && !this.props.getAudience().isBlank()) {
            claims.put("aud", this.props.getAudience());
        }
        return Jwts.builder().issuer(this.props.getIssuer()).subject(sub).issuedAt(Date.from(now)).expiration(Date.from(exp)).claims(claims).signWith((Key)this.key).compact();
    }

    @Override
    public Map<String, Object> parseAndValidate(String jwt) {
        try {
            Jws parsed = Jwts.parser().verifyWith(this.key).clockSkewSeconds((long)this.props.getClockSkewSeconds().intValue()).build().parseSignedClaims((CharSequence)jwt);
            Claims claims = (Claims)parsed.getPayload();
            String issuer = claims.getIssuer();
            if (issuer == null || !issuer.equals(this.props.getIssuer())) {
                throw new InvalidJwtTokenException("Issuer inv\u00e1lido");
            }
            if (this.props.getAudience() != null && !this.props.getAudience().isBlank()) {
                Collection col;
                Object audClaim = claims.get((Object)"aud");
                if (audClaim == null) {
                    throw new InvalidJwtTokenException("Audience ausente");
                }
                if (audClaim instanceof String) {
                    String s = (String)audClaim;
                    if (!this.props.getAudience().equals(s)) {
                        throw new InvalidJwtTokenException("Audience inv\u00e1lida");
                    }
                } else if (audClaim instanceof Collection && !(col = (Collection)audClaim).contains(this.props.getAudience())) {
                    throw new InvalidJwtTokenException("Audience inv\u00e1lida");
                }
            }
            return new HashMap<String, Object>((Map<String, Object>)claims);
        }
        catch (ExpiredJwtException e) {
            throw new TokenExpiredException("JWT expirado", e);
        }
        catch (SignatureException e) {
            throw new InvalidJwtTokenException("Assinatura inv\u00e1lida", e);
        }
        catch (RuntimeException e) {
            throw new InvalidJwtTokenException("Token JWT inv\u00e1lido", e);
        }
    }

    private String resolveSubject(TokenSubject subject) {
        Principal p;
        if (subject instanceof Principal && (p = (Principal)((Object)subject)).getName() != null) {
            return p.getName();
        }
        String[] c = new String[]{"getSubject", "subject", "getUsername", "username", "getEmail", "email", "getId", "id"};
        String[] stringArray = c;
        int n = c.length;
        int n2 = 0;
        while (n2 < n) {
            String m = stringArray[n2];
            try {
                Method mm = subject.getClass().getMethod(m, new Class[0]);
                Object v = mm.invoke((Object)subject, new Object[0]);
                if (v != null) {
                    return String.valueOf(v);
                }
            }
            catch (NoSuchMethodException noSuchMethodException) {
            }
            catch (Exception exception) {
                // empty catch block
            }
            ++n2;
        }
        return subject.toString();
    }
}

