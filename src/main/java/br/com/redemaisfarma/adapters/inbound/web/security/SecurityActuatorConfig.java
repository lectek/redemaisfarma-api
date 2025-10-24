/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
 *  org.springframework.boot.actuate.health.HealthEndpoint
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.annotation.Order
 *  org.springframework.security.config.Customizer
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer$AuthorizedUrl
 *  org.springframework.security.web.SecurityFilterChain
 *  org.springframework.security.web.util.matcher.RequestMatcher
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration(proxyBeanMethods=false)
public class SecurityActuatorConfig {
    @Bean
    @Order(value=0)
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http.securityMatcher((RequestMatcher)EndpointRequest.toAnyEndpoint());
        http.authorizeHttpRequests(auth -> ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new RequestMatcher[]{EndpointRequest.to((Class[])new Class[]{HealthEndpoint.class})})).permitAll().requestMatchers(new RequestMatcher[]{EndpointRequest.to((String[])new String[]{"loggers"})})).hasRole("DEV").anyRequest()).authenticated());
        http.csrf(csrf -> csrf.ignoringRequestMatchers(new RequestMatcher[]{EndpointRequest.toAnyEndpoint()}));
        http.httpBasic(Customizer.withDefaults());
        http.formLogin(form -> form.disable());
        return (SecurityFilterChain)http.build();
    }
}

