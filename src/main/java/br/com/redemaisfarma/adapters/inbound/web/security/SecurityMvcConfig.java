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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
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
            .cors(AbstractHttpConfigurer::disable) // use seu CorsFilter global; mude para withDefaults() se quiser habilitar aqui
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
                // recursos estáticos
                auth.requestMatchers(
                        "/assets/**", "/css/**", "/js/**", "/images/**",
                        "/webjars/**", "/static/**", "/favicon.ico",
                        "/index.html", "/robots.txt"
                ).permitAll();

                // pré-flight CORS
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                // páginas públicas
                auth.requestMatchers(
                        "/api/public/**",
                        "/", "/catalogo", "/produtos", "/carrinho", "/checkout",
                        "/sobre", "/login", "/logout", "/cadastro", "/cadastro-cliente",
                        "/clientes/cadastro", "/error"
                ).permitAll();

                // (REMOVIDO /pos-login)
                // se quiser manter /entrar-com e /limpar-escolha protegidas:
                auth.requestMatchers("/entrar-com", "/limpar-escolha")
                        .authenticated();

                // Swagger: liberado em dev, restrito em prod
                if (isProd()) {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                            .hasRole("ADMIN");
                } else {
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                            .permitAll();
                }

                // Actuator: em prod só admin (fora health/info), em dev tudo liberado
                if (isProd()) {
                    auth.requestMatchers("/actuator/health/**", "/actuator/info").permitAll();
                    auth.requestMatchers("/actuator/**").hasRole("ADMIN");
                } else {
                    auth.requestMatchers("/actuator/**").permitAll();
                }

                // fluxo de esqueci/resetar senha
                auth.requestMatchers(
                        "/auth/esqueci-senha", "/auth/esqueci-senha/**",
                        "/auth/resetar-senha", "/auth/resetar-senha/**",
                        "/cliente/auth/esqueci-senha", "/cliente/auth/esqueci-senha/**",
                        "/cliente/auth/resetar-senha", "/cliente/auth/resetar-senha/**"
                ).permitAll();

                auth.requestMatchers("/auth/mudar-senha", "/auth/mudar-senha/**")
                        .authenticated();

                // criação de cliente via POST
                auth.requestMatchers(HttpMethod.POST, "/clientes").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/login").permitAll();

                // (REMOVIDOS TODOS OS MATCHERS /admin E SUBROTAS)
                // área do cliente permanece protegida por ROLE_CLIENTE
                auth.requestMatchers("/cliente", "/cliente/**").hasRole("CLIENTE");

                // qualquer outra rota exige autenticação
                auth.anyRequest().authenticated();
            })
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("usuario")
                    .passwordParameter("senha")
                    // pós-login: usa apenas /login como entrada,
                    // e redireciona para /cliente como padrão
                    .defaultSuccessUrl("/cliente") // sem 'true' para respeitar SavedRequest
                    .failureUrl("/login?error")
                    .permitAll()
            )
            .logout(l -> l
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            )
            .rememberMe(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .sessionFixation(sf -> sf.migrateSession())
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
                            .maxAgeInSeconds(31_536_000))
                    .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );

        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    static class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain) throws ServletException, IOException {
            CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                // força criação do cookie XSRF-TOKEN
                token.getToken();
            }
            chain.doFilter(request, response);
        }
    }
}
