/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.cors.CorsConfiguration
 *  org.springframework.web.cors.CorsConfigurationSource
 *  org.springframework.web.cors.UrlBasedCorsConfigurationSource
 *  org.springframework.web.filter.CorsFilter
 */
package br.com.redemaisfarma.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebCorsConfig {
    @Bean(name={"corsFilter"})
    public CorsFilter corsFilter(@Value(value="${app.web.cors.allowed-origins:*}") String originsProp) {
        List origins = Arrays.stream(originsProp.split(",")).map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toList());
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setMaxAge(Long.valueOf(3600L));
        cfg.setAllowCredentials(Boolean.valueOf(true));
        if (origins.size() == 1 && "*".equals(origins.get(0))) {
            cfg.setAllowedOriginPatterns(List.of("*"));
        } else {
            cfg.setAllowedOrigins(origins);
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter((CorsConfigurationSource)source);
    }
}

