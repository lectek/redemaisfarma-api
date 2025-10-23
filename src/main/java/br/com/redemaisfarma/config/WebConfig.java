// src/main/java/br/com/redemaisfarma/config/WebConfig.java
package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class WebConfig {

    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.of("pt", "BR"));
        resolver.setCookieMaxAge(Duration.ofDays(30));
        resolver.setCookiePath("/");
        // resolver.setCookieName("LANG"); // opcional
        return resolver;
    }
}
