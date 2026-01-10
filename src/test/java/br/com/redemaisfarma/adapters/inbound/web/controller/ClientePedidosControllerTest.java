package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = ClientePedidosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientePedidosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private PaymentMethodService paymentMethodService;

    @MockBean
    private AppSettingService appSettingService;

    @MockBean
    private OtpServicePort otpServicePort;

    @MockBean
    private ThymeleafViewResolver thymeleafViewResolver;
    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cliente-test", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarUsaPrincipalQuandoUsuarioNaoEncontrado() throws Exception {
        String principal = "cliente@example.com";

        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(5L);
        pedido.setData(LocalDateTime.now().minusHours(2));
        pedido.setTotal(new BigDecimal("45.90"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Teste");
        pedido.setCliente(cliente);

        when(usuarioRepository.findByEmailOrCpf(principal)).thenReturn(Optional.empty());
        when(pedidoRepository.listarPorCliente(principal, principal)).thenReturn(List.of(pedido));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "N/A");

        MvcResult result = mockMvc.perform(get("/cliente/pedidos").principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cliente/pedidos/lista"))
                .andReturn();

        Object attr = result.getModelAndView().getModel().get("pedidos");
        assertNotNull(attr);
        List<?> pedidos = (List<?>) attr;
        assertEquals(1, pedidos.size());
    }
}
