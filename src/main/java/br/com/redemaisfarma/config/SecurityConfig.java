/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.security.config.Customizer
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer$AuthorizedUrl
 *  org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
 *  org.springframework.security.web.SecurityFilterChain
 *  org.springframework.security.web.authentication.AuthenticationSuccessHandler
 *  org.springframework.security.web.util.matcher.AntPathRequestMatcher
 *  org.springframework.security.web.util.matcher.RequestMatcher
 */
package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers(new RequestMatcher[]{new AntPathRequestMatcher("/api/public/**"), new AntPathRequestMatcher("/actuator/**")})).authorizeHttpRequests(auth -> ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/", "/home", "/sobre", "/contato", "/produtos/**", "/ofertas/**", "/login", "/logout", "/pos-login", "/entrar-com", "/limpar-escolha", "/css/**", "/js/**", "/images/**", "/img/**", "/webjars/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/public/**", "/actuator/health", "/actuator/info", "/actuator/mappings", "/actuator/loggers"})).permitAll().anyRequest()).authenticated()).formLogin(form -> ((FormLoginConfigurer)form.loginPage("/login").successHandler(this.posLoginSuccessHandler())).permitAll()).logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/home")).httpBasic(Customizer.withDefaults());
        return (SecurityFilterChain)http.build();
    }

    @Bean
    AuthenticationSuccessHandler posLoginSuccessHandler() {
        return (request, response, authentication) -> response.sendRedirect(request.getContextPath() + "/pos-login");
    }
}

