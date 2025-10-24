/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.security.core.authority.SimpleGrantedAuthority
 *  org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
 *  org.springframework.security.web.authentication.AuthenticationSuccessHandler
 *  org.springframework.util.StringUtils
 *  org.springframework.web.cors.CorsConfiguration
 *  org.springframework.web.cors.CorsConfigurationSource
 *  org.springframework.web.cors.UrlBasedCorsConfigurationSource
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods=false)
public class WebSecurityBeans {
    @Bean
    public GrantedAuthoritiesMapper authoritiesMapper() {
        return in -> {
            HashSet<SimpleGrantedAuthority> out = new HashSet<SimpleGrantedAuthority>(in);
            boolean isDev = in.stream().anyMatch(a -> "ROLE_DEVELOPER".equals(a.getAuthority()));
            boolean isAdmin = in.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            if (isDev) {
                out.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                out.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            } else if (isAdmin) {
                out.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            }
            return out;
        };
    }

    @Bean
    public AuthenticationSuccessHandler webSuccessHandler(@Value(value="${app.security.oauth2.post-login-redirect:/pos-login}") String redirect) {
        return (request, response, authentication) -> {
            response.setStatus(302);
            response.sendRedirect(redirect);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value(value="${cors.allowed-origins:}") String allowedOriginsProp) {
        List<String> allowedOrigins = StringUtils.hasText((String)allowedOriginsProp) ? Arrays.stream(allowedOriginsProp.split(",")).map(String::trim).filter(StringUtils::hasText).toList() : List.of("http://localhost:18090", "http://localhost:18080", "http://localhost:*", "https://*.redemaisfarma.com", "https://*.lek-tec.com");
        CorsConfiguration cfg = new CorsConfiguration();
        boolean hasWildcard = allowedOrigins.stream().anyMatch(o -> o.contains("*") || o.contains(":*"));
        if (hasWildcard) {
            cfg.setAllowedOriginPatterns(allowedOrigins);
        } else {
            cfg.setAllowedOrigins(allowedOrigins);
        }
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "X-OTP-TOKEN", "X-CSRF-TOKEN", "X-XSRF-TOKEN"));
        cfg.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN"));
        cfg.setAllowCredentials(Boolean.valueOf(true));
        cfg.setMaxAge(Long.valueOf(3600L));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}

