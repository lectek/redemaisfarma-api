package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.ProdutoAdminService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProdutoAdminPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoAdminPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoAdminService produtoAdminService;

    @MockBean
    private ProdutoRepository produtoRepository;

    @MockBean
    private AppSettingService appSettingService;

    @MockBean
    private ProdutoCategoriaRepository produtoCategoriaRepository;

    @MockBean
    private OtpServicePort otpServicePort;

    @MockBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("produto-admin", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void formRedirectsToNovo() throws Exception {
        mockMvc.perform(get("/admin/produtos/form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"));
    }
}
