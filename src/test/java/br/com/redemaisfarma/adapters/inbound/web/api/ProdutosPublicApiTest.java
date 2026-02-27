package br.com.redemaisfarma.adapters.inbound.web.api;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProdutosPublicApi.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutosPublicApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoJpaRepository repo;

    @MockBean
    private AppSettingService appSettingService;

    @Test
    void obterRetornaProdutoQuandoPublico() throws Exception {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(10L);
        entity.setNome("Dipirona 500mg");
        entity.setDescricao("Analgesico");
        entity.setPrecoVenda(BigDecimal.valueOf(12.50));
        entity.setCategoria("ANALGESICO");
        entity.setCodigoBarras("7891234567895");
        entity.setEstoque(9);
        entity.setDisponivel(true);
        entity.setDataCadastro(LocalDate.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(repo.findPublicById(10L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/public/produtos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value(10))
                .andExpect(jsonPath("$.nome").value("Dipirona 500mg"))
                .andExpect(jsonPath("$.situacao").value("ATIVO"));
    }

    @Test
    void obterRetornaNotFoundQuandoProdutoNaoEstaPublico() throws Exception {
        when(repo.findPublicById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/public/produtos/99"))
                .andExpect(status().isNotFound());
    }
}
