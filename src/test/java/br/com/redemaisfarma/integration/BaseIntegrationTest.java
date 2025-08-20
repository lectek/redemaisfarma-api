package br.com.redemaisfarma.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BaseIntegrationTest {
  @Test void contextLoads() { }
}
