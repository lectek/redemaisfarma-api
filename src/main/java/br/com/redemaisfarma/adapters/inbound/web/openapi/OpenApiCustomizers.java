package br.com.redemaisfarma.adapters.inbound.web.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiCustomizers {
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info()
      .title("RedeMaisFarma API")
      .version("v1")
      .description("API da RedeMaisFarma"));
  }
}
