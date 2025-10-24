/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.Filter
 *  jakarta.servlet.FilterChain
 *  jakarta.servlet.ServletException
 *  jakarta.servlet.ServletRequest
 *  jakarta.servlet.ServletResponse
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.core.annotation.Order
 *  org.springframework.core.env.Environment
 *  org.springframework.http.HttpMethod
 *  org.springframework.security.config.annotation.web.builders.HttpSecurity
 *  org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
 *  org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer$AuthorizedUrl
 *  org.springframework.security.config.http.SessionCreationPolicy
 *  org.springframework.security.web.SecurityFilterChain
 *  org.springframework.security.web.authentication.www.BasicAuthenticationFilter
 *  org.springframework.security.web.csrf.CookieCsrfTokenRepository
 *  org.springframework.security.web.csrf.CsrfToken
 *  org.springframework.security.web.csrf.CsrfTokenRepository
 *  org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter$ReferrerPolicy
 *  org.springframework.security.web.util.matcher.AntPathRequestMatcher
 *  org.springframework.security.web.util.matcher.RequestMatcher
 *  org.springframework.web.filter.OncePerRequestFilter
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration(proxyBeanMethods=false)
@Order(value=2)
public class SecurityMvcConfig {
    private final Environment env;

    public SecurityMvcConfig(Environment env) {
        this.env = env;
    }

    private boolean isDevLike() {
        return Arrays.stream(this.env.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("dev") || p.equalsIgnoreCase("local") || p.equalsIgnoreCase("docker"));
    }

    private boolean isProd() {
        return Arrays.stream(this.env.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));
    }

    @Bean
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        boolean dev = this.isDevLike();
        http.securityMatcher(new String[]{"/**"}).cors(cors -> {}).csrf(csrf -> {
            CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
            csrf.csrfTokenRepository((CsrfTokenRepository)repo);
            csrf.ignoringRequestMatchers(new RequestMatcher[]{new AntPathRequestMatcher("/admin/export/**"), new AntPathRequestMatcher("/login", "POST")});
            if (dev) {
                csrf.ignoringRequestMatchers(new RequestMatcher[]{new AntPathRequestMatcher("/admin/catalogo/sincronizar"), new AntPathRequestMatcher("/webhooks/**"), new AntPathRequestMatcher("/actuator/**")});
            }
        }).authorizeHttpRequests(auth -> {
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/assets/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/static/**", "/favicon.ico", "/index.html", "/robots.txt"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(HttpMethod.OPTIONS, new String[]{"/**"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/api/public/**", "/", "/catalogo", "/produtos", "/carrinho", "/checkout", "/sobre", "/login", "/logout", "/cadastro", "/cadastro-cliente", "/clientes/cadastro", "/error"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/pos-login", "/entrar-com", "/limpar-escolha"})).authenticated();
            if (this.isProd()) {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"})).hasRole("ADMIN");
            } else {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"})).permitAll();
            }
            if (this.isProd()) {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/actuator/health/**", "/actuator/info"})).permitAll();
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/actuator/**"})).hasRole("ADMIN");
            } else {
                ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/actuator/**"})).permitAll();
            }
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/auth/esqueci-senha", "/auth/esqueci-senha/**", "/auth/resetar-senha", "/auth/resetar-senha/**", "/cliente/auth/esqueci-senha", "/cliente/auth/esqueci-senha/**", "/cliente/auth/resetar-senha", "/cliente/auth/resetar-senha/**"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/auth/mudar-senha", "/auth/mudar-senha/**"})).authenticated();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(HttpMethod.POST, new String[]{"/clientes"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(HttpMethod.POST, new String[]{"/login"})).permitAll();
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/dev/**"})).hasRole("DEVELOPER");
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/admin", "/admin/**"})).hasRole("ADMIN");
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/cliente", "/cliente/**"})).hasRole("CLIENTE");
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/admin/financeiro/**"})).hasAnyRole(new String[]{"ADMIN", "FINANCEIRO"});
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/admin/catalogo/**"})).hasAnyRole(new String[]{"ADMIN", "CATALOGO"});
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.requestMatchers(new String[]{"/admin/suporte/**"})).hasAnyRole(new String[]{"ADMIN", "SUPORTE"});
            ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)auth.anyRequest()).authenticated();
        }).formLogin(form -> {
            form.loginPage("/login");
            form.loginProcessingUrl("/login");
            form.usernameParameter("usuario");
            form.passwordParameter("senha");
            form.defaultSuccessUrl("/pos-login", true);
            form.failureUrl("/login?error");
            form.permitAll();
        }).logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true").invalidateHttpSession(true).deleteCookies(new String[]{"JSESSIONID", "XSRF-TOKEN"})).rememberMe(AbstractHttpConfigurer::disable).sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).sessionFixation(sessionFixation -> sessionFixation.migrateSession())).headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'self'")).frameOptions(frame -> frame.sameOrigin()).httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true).maxAgeInSeconds(31536000L)).referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        http.addFilterAfter((Filter)new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        return (SecurityFilterChain)http.build();
    }

    static class CsrfCookieFilter
    extends OncePerRequestFilter {
        CsrfCookieFilter() {
        }

        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
            CsrfToken token = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                token.getToken();
            }
            chain.doFilter((ServletRequest)request, (ServletResponse)response);
        }
    }
}

