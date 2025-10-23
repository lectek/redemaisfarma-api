package br.com.redemaisfarma.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI redeMaisFarmaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RedeMaisFarma API")
                .version("v1.0.0")
                .description("Plataforma da RedeMaisFarma — catálogos, pedidos, clientes e integrações.")
                .license(new License().name("Apache 2.0")))
            .servers(List.of(
                new Server().url("http://127.0.0.1:18090").description("Local (Docker)"),
                new Server().url("http://localhost:8080").description("Local (sem Docker)"),
                new Server().url("https://api.seudominio.com").description("Produção")
            ))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
