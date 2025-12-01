// src/main/java/br/com/redemaisfarma/config/SecurityConfig.java
package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 🔑 Depois de logar:
        // - Se o usuário tentou acessar algo protegido (ex.: /checkout),
        //   ele volta para essa URL.
        // - Se não tiver URL anterior, cai na home "/".
        var successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/");

        http
            // CSRF desabilitado para algumas rotas técnicas / públicas de API
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/actuator/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/api/public/**"
                )
            )

            .authorizeHttpRequests(auth -> auth

                // 🔹 Recursos estáticos SEMPRE liberados
                .requestMatchers(
                    "/assets/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/img/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()

                // 🔹 PÁGINAS PÚBLICAS (área cliente / vitrine)
                // Qualquer pessoa pode navegar na loja:
                // - Home / (raiz)
                // - /cliente      (alias da home cliente)
                // - /cliente/index
                // - /sobre        (página institucional)
                // - /produtos/**  (listar/detalhar produtos)
                // - /buscar       (busca de produtos)
                .requestMatchers(
                    "/",
                    "/cliente",
                    "/cliente/index",
                    "/sobre",
                    "/produtos/**",
                    "/buscar"
                ).permitAll()

                // 🔹 Telas de login/cadastro (GET e POST)
                // Liberadas para o usuário poder entrar ou criar conta
                .requestMatchers(HttpMethod.GET,
                    "/auth/login",
                    "/auth/cadastro-cliente"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                    "/auth/login",
                    "/auth/cadastro-cliente"
                ).permitAll()

                // 🔹 APIs públicas e health (para monitoramento, docs, etc.)
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // 🔸 ROTAS DE COMPRA — exigem cadastro/login
                // Tudo que mexe com pedido, checkout e carrinho é protegido.
                .requestMatchers(
                    "/carrinho/**",
                    "/checkout/**",
                    "/pedido/**"
                ).authenticated()

                // 🔸 ÁREA ADMIN — só para usuários com ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 🔒 Qualquer outra rota que sobrar exige login
                .anyRequest().authenticated()
            )

            // 🔐 Configuração do login via formulário
            .formLogin(f -> f
                .loginPage("/auth/login")          // GET: tela de login
                .loginProcessingUrl("/auth/login") // POST: envio do formulário
                .successHandler(successHandler)    // redireciona para URL original ou "/"
                .failureUrl("/auth/login?error")
                .permitAll()
            )

            // 🔓 Logout padrão
            .logout(l -> l
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
