package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private ImageStorageService imageStorageService;

    @MockBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("produto-admin", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(produtoRepository.searchNaoDisponiveis(any(), any(Pageable.class)))
                .thenReturn(Page.empty());
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

    @Test
    void naoProntosTodosPageReturnsOk() throws Exception {
        mockMvc.perform(get("/admin/produtos/nao-prontos/todos"))
                .andExpect(status().isOk());
    }

    @Test
    void naoProntosTodosPageRetornaPendentesDoBanco() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(77L);
        produto.setLegacyId(700L);
        produto.setNome("Amoxicilina");
        produto.setDescricao("Amoxicilina 500mg");
        produto.setCategoria("Estoque fisico");
        produto.setCodigoBarras("7890007770001");
        produto.setEstoque(5);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(produto), PageRequest.of(0, 1000), 1);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/produtos/nao-prontos/todos").param("q", "amoxi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingTotal").value(1))
                .andExpect(jsonPath("$.pendingItems[0].id").value(77))
                .andExpect(jsonPath("$.pendingItems[0].origem").value("CATALOGO_PENDENTE"))
                .andExpect(jsonPath("$.pendingItems[0].nome").value("Amoxicilina"));
    }

    @Test
    void naoProntosEndpointRetornaItensDoBanco() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(10L);
        produto.setLegacyId(123L);
        produto.setNome("Dipirona");
        produto.setDescricao("Dipirona 500mg");
        produto.setCategoria("Estoque fisico");
        produto.setCodigoBarras("7890001112223");
        produto.setEstoque(9);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(produto), PageRequest.of(0, 12), 1);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "dip")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(10))
                .andExpect(jsonPath("$.items[0].origem").value("CATALOGO_PENDENTE"))
                .andExpect(jsonPath("$.items[0].nome").value("Dipirona"));
    }

    @Test
    void naoProntosEndpointRetornaPaginacaoDoBanco() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(11L);
        produto.setNome("Nimesulida");
        produto.setCategoria("Estoque fisico");
        produto.setEstoque(3);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(produto), PageRequest.of(0, 1), 2);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "nim")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void naoProntosEndpointUsaFallbackQuandoCategoriaNaoRetornaItens() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(33L);
        produto.setNome("Ibuprofeno");
        produto.setCategoria("Estoque Físico");
        produto.setEstoque(2);

        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(produtoRepository.searchNaoDisponiveis(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(produto), PageRequest.of(0, 12), 1));

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "ibu")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(33))
                .andExpect(jsonPath("$.items[0].nome").value("Ibuprofeno"));
    }

    @Test
    void naoProntosEndpointNormalizaFiltroEmBrancoParaNull() throws Exception {
        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "   ")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk());

        verify(produtoRepository).searchNaoDisponiveisByCategoria(
                isNull(),
                eq("Estoque fisico"),
                any(Pageable.class)
        );
    }

    @Test
    void importarEstoqueFisicoRedirectsToNovo() throws Exception {
        mockMvc.perform(post("/admin/produtos/importar-estoque-fisico"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"));
    }

    @Test
    void publicarAptosEmLotePublicaSomenteProdutosComDadosMinimos() throws Exception {
        ProdutoEntity apto = new ProdutoEntity();
        apto.setId(1L);
        apto.setNome("Produto apto");
        apto.setPrecoVenda(BigDecimal.valueOf(11.90));
        apto.setEstoque(7);
        apto.setImagem("https://cdn.exemplo.com/p1.png");
        apto.setDisponivel(false);
        apto.setStatus(ProdutoStatus.IMPORTADO);

        ProdutoEntity bloqueado = new ProdutoEntity();
        bloqueado.setId(2L);
        bloqueado.setNome("Produto sem imagem");
        bloqueado.setPrecoVenda(BigDecimal.valueOf(9.90));
        bloqueado.setEstoque(10);
        bloqueado.setImagem(null);
        bloqueado.setDisponivel(false);
        bloqueado.setStatus(ProdutoStatus.IMPORTADO);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(apto, bloqueado), PageRequest.of(0, 1000), 2);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(produtoRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(apto, bloqueado));

        mockMvc.perform(post("/admin/produtos/nao-prontos/publicar-aptos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/nao-prontos/todos"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProdutoEntity>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(produtoRepository).saveAll(captor.capture());

        List<ProdutoEntity> publicados = captor.getValue();
        assertThat(publicados).hasSize(1);
        ProdutoEntity publicado = publicados.get(0);
        assertThat(publicado.getId()).isEqualTo(1L);
        assertThat(publicado.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
        assertThat(publicado.getDisponivel()).isTrue();
        assertThat(publicado.getPublicadoEm()).isNotNull();
    }

    @Test
    void publicarAptosEmLoteNaoSalvaQuandoNaoHaPendentes() throws Exception {
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(post("/admin/produtos/nao-prontos/publicar-aptos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/nao-prontos/todos"));

        verify(produtoRepository, never()).saveAll(any());
    }
}
