package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AdminPedidosController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminPedidosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test-admin", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private PaymentMethodService paymentMethodService;

    @MockBean
    private AppSettingService appSettingService;

    @MockBean
    private OtpServicePort otpServicePort;

    @MockBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @Test
    void listarCarregaPedidosNoModel() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(10L);
        pedido.setData(LocalDateTime.now().minusDays(1));
        pedido.setTotal(new BigDecimal("120.50"));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Maria Silva");
        pedido.setCliente(cliente);

        when(pedidoRepository.listarRecentes(any(Pageable.class))).thenReturn(List.of(pedido));

        MvcResult result = mockMvc.perform(get("/admin/pedidos").param("status", "AGUARDANDO"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/pedidos/lista"))
                .andReturn();

        Object attr = result.getModelAndView().getModel().get("pedidos");
        assertNotNull(attr);
        List<?> pedidos = (List<?>) attr;
        assertEquals(1, pedidos.size());

        AdminPedidosController.PedidoListaView viewModel =
                (AdminPedidosController.PedidoListaView) pedidos.get(0);
        assertEquals("Maria Silva", viewModel.clienteNome());
        assertEquals("AGUARDANDO_PAGAMENTO", viewModel.status());
        assertEquals("Aguardando pagamento", viewModel.statusLabel());
    }
}
