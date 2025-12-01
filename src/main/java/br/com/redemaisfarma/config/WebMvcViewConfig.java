// src/main/java/br/com/redemaisfarma/config/WebMvcViewConfig.java
package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcViewConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // GET /login renderiza o template Thymeleaf:
        // src/main/resources/templates/pages/auth/login.html
        registry.addViewController("/login").setViewName("pages/auth/login");
    }
}
