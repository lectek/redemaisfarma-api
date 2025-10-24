/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.annotation.Order
 *  org.springframework.security.config.Customizer
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer$AuthorizedUrl
 *  org.springframework.security.config.http.SessionCreationPolicy
 *  org.springframework.security.web.SecurityFilterChain
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Order(value=0)
public class SecurityExportConfig {
    @Bean
    public SecurityFilterChain exportChain(HttpSecurity http) throws Exception {
        http.securityMatcher(new String[]{"/admin/export/**"}).cors(cors -> {}).csrf(csrf -> csrf.disable()).sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.anyRequest()).hasRole("ADMIN")).httpBasic(Customizer.withDefaults());
        return (SecurityFilterChain)http.build();
    }
}

