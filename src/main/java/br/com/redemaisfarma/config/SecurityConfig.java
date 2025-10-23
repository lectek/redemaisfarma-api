package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

  @Bean
  @Order(200)
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.ignoringRequestMatchers(
        new AntPathRequestMatcher("/api/public/**"),
        new AntPathRequestMatcher("/actuator/**")
      ))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/",                       // root (controller decide)
          "/auth/login",             // login GET/POST
          "/auth/cadastro-cliente",  // cadastro público
          "/auth/esqueci-senha", "/auth/mudar-senha",
          "/entrar-com", "/limpar-escolha",
          "/css/**", "/js/**", "/images/**", "/img/**", "/webjars/**",
          "/swagger-ui/**", "/v3/api-docs/**",
          "/api/public/**",
          "/actuator/health", "/actuator/info", "/actuator/mappings", "/actuator/loggers"
        ).permitAll()
        .requestMatchers("/pos-login").authenticated()
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/auth/login")            // GET
        .loginProcessingUrl("/auth/login")   // POST
        .usernameParameter("usuario")
        .passwordParameter("senha")
        .successHandler(posLoginSuccessHandler())  // sempre manda para /pos-login
        .permitAll()
      )
      .rememberMe(rm -> rm
        .rememberMeParameter("remember-me")
        .key("rede-mais-farma-remember-me-key")
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/auth/login?logout")
      )
      .httpBasic(Customizer.withDefaults());

    return http.build();
  }

  @Bean
  AuthenticationSuccessHandler posLoginSuccessHandler() {
    return (request, response, authentication) ->
      response.sendRedirect(request.getContextPath() + "/pos-login");
  }
}
