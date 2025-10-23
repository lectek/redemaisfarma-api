// src/main/java/br/com/redemaisfarma/config/MethodSecurityConfig.java
package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(prePostEnabled = false, securedEnabled = false, jsr250Enabled = false)
public class MethodSecurityConfig { }
