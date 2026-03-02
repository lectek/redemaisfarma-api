package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentosConfigControllerTest {

    @Mock
    private AppSettingService settings;

    private ObjectMapper objectMapper;
    private PagamentosConfigController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new PagamentosConfigController(settings, objectMapper);

        lenient().when(settings.getOrDefault(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getBoolean(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getInt(anyString(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getDecimal(anyString(), any(BigDecimal.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    void editarPopulatesModelUsingStoredSettings() {
        when(settings.getOrDefault("pg.gateway", "mercadopago")).thenReturn("pagarme");
        when(settings.getBoolean("pg.pix_ativo", true)).thenReturn(false);
        when(settings.getBoolean("pg.boleto_ativo", false)).thenReturn(true);
        when(settings.getDecimal("pg.taxa_cartao", BigDecimal.ZERO)).thenReturn(new BigDecimal("2.49"));
        when(settings.getInt("pg.max_parcelas", 6)).thenReturn(10);
        when(settings.getDecimal("pg.parcela_min", BigDecimal.valueOf(20L))).thenReturn(new BigDecimal("35.00"));
        when(settings.getOrDefault("pg.webhook_url", "")).thenReturn("https://api.exemplo.com/webhook");
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn(
                "[{\"id\":\"m1\",\"nome\":\"Convenio\",\"tipo\":\"voucher\",\"taxa\":1.5,\"ativo\":true}]"
        );

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editar(model);

        Assertions.assertThat(view).isEqualTo("pages/admin/configuracoes/pagamentos");
        PagamentosConfigController.PagamentosForm cfg =
                (PagamentosConfigController.PagamentosForm) model.get("cfg");
        Assertions.assertThat(cfg).isNotNull();
        Assertions.assertThat(cfg.getGateway()).isEqualTo("pagarme");
        Assertions.assertThat(cfg.getPixAtivo()).isFalse();
        Assertions.assertThat(cfg.getBoletoAtivo()).isTrue();
        Assertions.assertThat(cfg.getTaxacartao()).isEqualByComparingTo("2.49");
        Assertions.assertThat(cfg.getMaxParcelas()).isEqualTo(10);
        Assertions.assertThat(cfg.getParcelaMin()).isEqualByComparingTo("35.00");
        Assertions.assertThat(cfg.getWebhookUrl()).isEqualTo("https://api.exemplo.com/webhook");
        Assertions.assertThat(model.get("webhookConfigured")).isEqualTo(true);
        Assertions.assertThat(model.get("novoMetodo"))
                .isInstanceOf(PagamentosConfigController.MetodoForm.class);
        List<?> customMethods = (List<?>) model.get("customMethods");
        Assertions.assertThat(customMethods).hasSize(1);
        PagamentosConfigController.MetodoPagamento metodo =
                (PagamentosConfigController.MetodoPagamento) customMethods.get(0);
        Assertions.assertThat(metodo.getNome()).isEqualTo("Convenio");
    }

    @Test
    void editarReturnsEmptyCustomMethodsWhenJsonIsInvalid() {
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn("{json-invalido}");

        ExtendedModelMap model = new ExtendedModelMap();
        controller.editar(model);

        List<?> customMethods = (List<?>) model.get("customMethods");
        Assertions.assertThat(customMethods).isEmpty();
    }

    @Test
    void salvarPersistsAllPaymentSettings() {
        PagamentosConfigController.PagamentosForm cfg =
                new PagamentosConfigController.PagamentosForm();
        cfg.setGateway("  pagarme  ");
        cfg.setPixAtivo(true);
        cfg.setCartaoAtivo(false);
        cfg.setBoletoAtivo(true);
        cfg.setDinheiroAtivo(null);
        cfg.setTaxacartao(new BigDecimal("2.35"));
        cfg.setMaxParcelas(12);
        cfg.setParcelaMin(new BigDecimal("30.50"));
        cfg.setPublicKey(" pub-key ");
        cfg.setSecretKey(null);
        cfg.setWebhookUrl("https://hooks.exemplo.com");
        cfg.setPixChave("pix-chave");
        cfg.setPixTipo("cpf");
        cfg.setDinheiroMinimo(new BigDecimal("15.00"));
        cfg.setDinheiroTrocoMax(BigDecimal.ZERO);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Configuracoes de pagamento atualizadas.");
        verify(settings, times(15)).upsert(anyString(), anyString(), anyString());
        verify(settings).upsert("pg.gateway", "pagarme", "Gateway ativo");
        verify(settings).upsert("pg.pix_ativo", "true", "PIX ativo");
        verify(settings).upsert("pg.cartao_ativo", "false", "Cartao ativo");
        verify(settings).upsert("pg.dinheiro_ativo", "false", "Dinheiro ativo");
        verify(settings).upsert("pg.max_parcelas", "12", "Maximo de parcelas");
        verify(settings).upsert("pg.taxa_cartao", "2.35", "Taxa de cartao");
        verify(settings).upsert("pg.secret_key", "", "Secret key gateway");
        verify(settings).upsert("pg.dinheiro_min", "15.00", "Valor minimo para dinheiro");
    }

    @Test
    void adicionarMetodoReturnsErrorWhenBindingHasErrors() {
        PagamentosConfigController.MetodoForm form =
                new PagamentosConfigController.MetodoForm();
        form.setNome("");
        BindingResult br = org.mockito.Mockito.mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.adicionarMetodo(form, br, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("error");
        Assertions.assertThat(attrs.getFlashAttributes().get("error"))
                .isEqualTo("Preencha o nome do metodo.");
        verify(settings, never()).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void adicionarMetodoReturnsErrorWhenMethodAlreadyExists() {
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn(
                "[{\"id\":\"m1\",\"nome\":\"Pix parcelado\",\"tipo\":\"pix\",\"taxa\":0,\"ativo\":true}]"
        );
        PagamentosConfigController.MetodoForm form =
                new PagamentosConfigController.MetodoForm();
        form.setNome("  pix PARCELADO ");
        BindingResult br = org.mockito.Mockito.mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.adicionarMetodo(form, br, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("error");
        Assertions.assertThat(attrs.getFlashAttributes().get("error"))
                .isEqualTo("Metodo ja existe.");
        verify(settings, never()).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void adicionarMetodoPersistsNewMethodWithDefaultAtivo() throws Exception {
        PagamentosConfigController.MetodoForm form =
                new PagamentosConfigController.MetodoForm();
        form.setNome("  Carteira Digital ");
        form.setTipo(null);
        form.setTaxa(new BigDecimal("1.99"));
        form.setAtivo(null);
        BindingResult br = org.mockito.Mockito.mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.adicionarMetodo(form, br, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Metodo adicionado.");
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(settings).upsert(
                org.mockito.Mockito.eq("pg.custom_methods"),
                jsonCaptor.capture(),
                org.mockito.Mockito.eq("Metodos de pagamento personalizados")
        );
        List<PagamentosConfigController.MetodoPagamento> saved = objectMapper.readValue(
                jsonCaptor.getValue(),
                new TypeReference<List<PagamentosConfigController.MetodoPagamento>>() {
                }
        );
        Assertions.assertThat(saved).hasSize(1);
        PagamentosConfigController.MetodoPagamento method = saved.get(0);
        Assertions.assertThat(method.getId()).isNotBlank();
        Assertions.assertThat(method.getNome()).isEqualTo("Carteira Digital");
        Assertions.assertThat(method.getTipo()).isEmpty();
        Assertions.assertThat(method.getTaxa()).isEqualByComparingTo("1.99");
        Assertions.assertThat(method.isAtivo()).isTrue();
    }

    @Test
    void removerMetodoRemovesMatchingIdAndPersistsRemainingOnes() throws Exception {
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn(
                "[{\"id\":\"m1\",\"nome\":\"PIX\",\"tipo\":\"pix\",\"taxa\":0,\"ativo\":true},"
                        + "{\"id\":\"m2\",\"nome\":\"Dinheiro\",\"tipo\":\"cash\",\"taxa\":0,\"ativo\":true}]"
        );
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.removerMetodo("m1", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Metodo removido.");
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(settings).upsert(
                org.mockito.Mockito.eq("pg.custom_methods"),
                jsonCaptor.capture(),
                org.mockito.Mockito.eq("Metodos de pagamento personalizados")
        );
        List<PagamentosConfigController.MetodoPagamento> saved = objectMapper.readValue(
                jsonCaptor.getValue(),
                new TypeReference<List<PagamentosConfigController.MetodoPagamento>>() {
                }
        );
        Assertions.assertThat(saved).hasSize(1);
        Assertions.assertThat(saved.get(0).getId()).isEqualTo("m2");
    }
}
