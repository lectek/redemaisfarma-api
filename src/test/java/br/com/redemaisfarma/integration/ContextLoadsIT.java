package br.com.redemaisfarma.integration;

import br.com.redemaisfarma.TestBootApp;
import br.com.redemaisfarma.TestBootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
    classes = TestBootApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@Import(TestBootConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ContextLoadsIT {

  @Autowired
  private MockMvc mvc;

  @Test
  @DisplayName("Contexto do Spring sobe e expõe MockMvc")
  void contextoSobe() {
    assertNotNull(mvc, "MockMvc não foi injetado — contexto não subiu corretamente.");
  }
}
