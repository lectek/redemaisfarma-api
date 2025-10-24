/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.Filter
 *  org.springframework.beans.factory.ObjectProvider
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.annotation.Order
 *  org.springframework.core.env.Environment
 *  org.springframework.http.HttpMethod
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer$AuthorizedUrl
 *  org.springframework.security.config.http.SessionCreationPolicy
 *  org.springframework.security.web.AuthenticationEntryPoint
 *  org.springframework.security.web.SecurityFilterChain
 *  org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import br.com.redemaisfarma.adapters.inbound.web.security.JwtAuthEntryPoint;
import br.com.redemaisfarma.adapters.inbound.web.security.JwtOncePerRequestFilter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Order(value=1)
public class SecurityApiConfig {
    private final ObjectProvider<JwtOncePerRequestFilter> jwtFilterProvider;
    private final ObjectProvider<JwtAuthEntryPoint> jwtEntryPointProvider;
    private final Environment env;

    public SecurityApiConfig(ObjectProvider<JwtOncePerRequestFilter> jwtFilterProvider, ObjectProvider<JwtAuthEntryPoint> jwtEntryPointProvider, Environment env) {
        this.jwtFilterProvider = jwtFilterProvider;
        this.jwtEntryPointProvider = jwtEntryPointProvider;
        this.env = env;
    }

    private boolean isDevLike() {
        for (String p : this.env.getActiveProfiles()) {
            if (!"dev".equalsIgnoreCase(p) && !"local".equalsIgnoreCase(p) && !"docker".equalsIgnoreCase(p)) continue;
            return true;
        }
        return false;
    }

    private boolean isJwtEnabled() {
        return (Boolean)this.env.getProperty("security.jwt.enabled", Boolean.class, (Object)((Boolean)this.env.getProperty("jwt.enabled", Boolean.class, (Object)false)));
    }

    @Bean
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http.securityMatcher(new String[]{"/api/**"}).cors(cors -> {}).csrf(csrf -> csrf.disable()).sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> {
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/ping/**", "/api/status", "/api/auth/otp/**", "/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/email-claim/**", "/api/auth/password/reset-otp", "/api/auth/register/complete-otp"})).permitAll();
            if (this.isDevLike()) {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/dev/provision/start"})).permitAll();
            } else {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/dev/provision/start"})).hasRole("DEVELOPER");
            }
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/dev/provision/verify"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(HttpMethod.GET, new String[]{"/api/public/**", "/api/produtos/**"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/admin/**"})).hasRole("ADMIN");
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/financeiro/**"})).hasAnyRole(new String[]{"ADMIN", "FINANCEIRO"});
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/catalogo/**"})).hasAnyRole(new String[]{"ADMIN", "CATALOGO"});
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/suporte/**"})).hasAnyRole(new String[]{"ADMIN", "SUPORTE"});
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(HttpMethod.OPTIONS, new String[]{"/**"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.anyRequest()).authenticated();
        });
        if (this.isJwtEnabled()) {
            JwtOncePerRequestFilter jwtFilter;
            JwtAuthEntryPoint entryPoint = (JwtAuthEntryPoint)this.jwtEntryPointProvider.getIfAvailable();
            if (entryPoint != null) {
                http.exceptionHandling(ex -> ex.authenticationEntryPoint((AuthenticationEntryPoint)entryPoint));
            }
            if ((jwtFilter = (JwtOncePerRequestFilter)((Object)this.jwtFilterProvider.getIfAvailable())) != null) {
                http.addFilterBefore((Filter)jwtFilter, UsernamePasswordAuthenticationFilter.class);
            }
        }
        return (SecurityFilterChain)http.build();
    }
}

