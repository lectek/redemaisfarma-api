package br.com.redemaisfarma;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Boot mínimo para testes: habilita autoconfig e faz scan do app.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "br.com.redemaisfarma")
public class TestBootApp {
}
