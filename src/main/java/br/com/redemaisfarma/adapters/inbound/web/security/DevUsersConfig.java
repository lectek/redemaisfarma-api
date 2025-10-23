// src/main/java/br/com/redemaisfarma/adapters/inbound/web/security/DevUsersConfig.java
package br.com.redemaisfarma.adapters.inbound.web.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile({"dev","local","docker"})
public class DevUsersConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService inMemoryUsers(PasswordEncoder enc) {
    String senha = "@Lektec!larissa25";

    return new InMemoryUserDetailsManager(
        User.withUsername("dev")
            .password(enc.encode(senha))
            .roles("DEV")
            .build(),
        User.withUsername("admin")
            .password(enc.encode(senha))
            .roles("ADMIN")
            .build(),
        User.withUsername("lektecjava@gmail.com")
            .password(enc.encode(senha))
            .roles("DEV")
            .build()
    );
  }
}

