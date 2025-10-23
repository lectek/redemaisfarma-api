// src/main/java/br/com/redemaisfarma/adapters/inbound/web/security/SecurityMvcConfig.java
package br.com.redemaisfarma.adapters.inbound.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Configuration(proxyBeanMethods = false)
@Order(2)
public class SecurityMvcConfig {

    private final Environment env;

    public SecurityMvcConfig(Environment env) {
        this.env = env;
    }

    private boolean isDevLike() {
        return Arrays.stream(env.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("dev")
                        || p.equalsIgnoreCase("local")
                        || p.equalsIgnoreCase("docker"));
    }

    private boolean isProd() {
        return Arrays.stream(env.getActiveProfiles())
                .anyMatch(p -> p.equalsIgnoreCase("prod")
                        || p.equalsIgnoreCase("production"));
    }

    @Bean
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        boolean dev = isDevLike();

        http.securityMatcher("/**")
            .cors(cors -> {})
            .csrf(csrf -> {
                CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
                csrf.csrfTokenRepository(repo);
                csrf.ignoringRequestMatchers(
                    new AntPathRequestMatcher("/admin/export/**"),
                    new AntPathRequestMatcher("/login", "POST")
                );
                if (dev) {
                    csrf.ignoringRequestMatchers(
                        new AntPathRequestMatcher("/admin/catalogo/sincronizar"),
                        new AntPathRequestMatcher("/webhooks/**"),
                        new AntPathRequestMatcher("/actuator/**")
                    );
                }
            })
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                        "/assets/**", "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/static/**", "/favicon.ico", "/index.html", "/robots.txt"
                ).permitAll();

                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                // Páginas públicas
                auth.requestMatchers(
                        "/api/public/**",
                        "/", "/catalogo", "/produtos", "/carrinho", "/checkout", "/sobre",
                        "/login", "/logout",
                        "/cadastro", "/cadastro-cliente", "/clientes/cadastro",
                        "/error"
                ).permitAll();

                // Fluxos pós-login (requer sessão)
                auth.requestMatchers("/pos-login", "/entrar-com", "/limpar-escolha").authenticated();

                // Swagger (MVC)
                if (isProd()) {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").hasRole("ADMIN");
                } else {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                }

                // Actuator
                if (isProd()) {
                    auth.requestMatchers("/actuator/health/**", "/actuator/info").permitAll();
                    auth.requestMatchers("/actuator/**").hasRole("ADMIN");
                } else {
                    auth.requestMatchers("/actuator/**").permitAll();
                }

                // Recuperação de senha (público)
                auth.requestMatchers(
                    "/auth/esqueci-senha", "/auth/esqueci-senha/**",
                    "/auth/resetar-senha", "/auth/resetar-senha/**",
                    "/cliente/auth/esqueci-senha", "/cliente/auth/esqueci-senha/**",
                    "/cliente/auth/resetar-senha", "/cliente/auth/resetar-senha/**"
                ).permitAll();

                // Alterar senha (somente autenticado)
                auth.requestMatchers("/auth/mudar-senha", "/auth/mudar-senha/**").authenticated();

                // Submissão de cadastro
                auth.requestMatchers(HttpMethod.POST, "/clientes").permitAll();

                // POST do form login
                auth.requestMatchers(HttpMethod.POST, "/login").permitAll();

                // Perfis/áreas
                auth.requestMatchers("/dev/**").hasRole("DEVELOPER");
                auth.requestMatchers("/admin", "/admin/**").hasRole("ADMIN");
                auth.requestMatchers("/cliente", "/cliente/**").hasRole("CLIENTE");
                auth.requestMatchers("/admin/financeiro/**").hasAnyRole("ADMIN","FINANCEIRO");
                auth.requestMatchers("/admin/catalogo/**").hasAnyRole("ADMIN","CATALOGO");
                auth.requestMatchers("/admin/suporte/**").hasAnyRole("ADMIN","SUPORTE");

                auth.anyRequest().authenticated();
            })
            .formLogin(form -> {
                form.loginPage("/login");
                form.loginProcessingUrl("/login");
                form.usernameParameter("usuario");
                form.passwordParameter("senha");

                // força o redirect para /pos-login após autenticar
                form.defaultSuccessUrl("/pos-login", true);

                form.failureUrl("/login?error");
                form.permitAll();
            })
            .logout(l -> l
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            )
            .rememberMe(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: blob:; " +
                    "font-src 'self' data:; " +
                    "connect-src 'self'; " +
                    "frame-ancestors 'self'"
                ))
                .frameOptions(frame -> frame.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .preload(true)
                    .maxAgeInSeconds(31536000))
                .referrerPolicy(rp -> rp.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );

        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    /** Materializa o cookie XSRF-TOKEN nas respostas MVC */
    static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain) throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) { token.getToken(); } // touch
            chain.doFilter(request, response);
        }
    }
}
