package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminConfiguracoesGeralControllerTest {

    @Mock
    private AppSettingService settings;

    private AdminConfiguracoesGeralController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminConfiguracoesGeralController(settings);
    }

    @Test
    void formAppliesLegacyFallbackAndDefaultValues() {
        Map<String, String> persisted = new HashMap<>();
        persisted.put("GERAL.nome_sistema", "Rede Mais Farma");
        persisted.put("contato.email", "contato@redemaisfarma.com");
        persisted.put("preferencias.cadastro_rapido", "false");
        persisted.put("GERAL.habilitar_assinaturas", "yes");
        persisted.put("retirada.ativa", "1");
        persisted.put("app.estoque.alerta.enabled", "on");
        persisted.put("app.estoque.alerta.limite", "invalido");
        persisted.put("app.estoque.alerta.cooldown-minutes", "75");
        when(settings.getAllByKeys(anyCollection())).thenReturn(persisted);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.form(model);

        Assertions.assertThat(view).isEqualTo("pages/admin/configuracoes/geral");
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                (AdminConfiguracoesGeralController.ConfigGeralForm) model.get("cfg");
        Assertions.assertThat(cfg).isNotNull();
        Assertions.assertThat(cfg.getNomeSistema()).isEqualTo("Rede Mais Farma");
        Assertions.assertThat(cfg.getEmail()).isEqualTo("contato@redemaisfarma.com");
        Assertions.assertThat(cfg.getHabilitarCadastrorapido()).isFalse();
        Assertions.assertThat(cfg.getHabilitarAssinaturas()).isTrue();
        Assertions.assertThat(cfg.getRetiradaAtiva()).isTrue();
        Assertions.assertThat(cfg.getAlertaEstoqueAtivo()).isTrue();
        Assertions.assertThat(cfg.getAlertaEstoqueLimite()).isEqualTo("10");
        Assertions.assertThat(cfg.getAlertaEstoqueCooldown()).isEqualTo("75");
        Assertions.assertThat(cfg.getAlertaEstoqueCron()).isEqualTo("0 */30 * * * *");
    }

    @Test
    void salvarContatoPersistsOnlyContactSection() {
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                new AdminConfiguracoesGeralController.ConfigGeralForm();
        cfg.setEmail("contato@redemaisfarma.com");
        cfg.setTelefone("(11) 99999-1111");
        cfg.setWhatsapp("(11) 98888-7777");
        cfg.setInstagram("@redemais");
        cfg.setSiteUrl("https://redemaisfarma.com");
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, null, null, "contato", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/geral");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Configuracoes gerais atualizadas.");
        verify(settings).upsert(
                "contato.email",
                "contato@redemaisfarma.com",
                "Email principal"
        );
        verify(settings).upsert("contato.telefone", "(11) 99999-1111", "Telefone principal");
        verify(settings).upsert("contato.whatsapp", "(11) 98888-7777", "Whatsapp");
        verify(settings).upsert("contato.instagram", "@redemais", "Instagram");
        verify(settings).upsert("contato.site_url", "https://redemaisfarma.com", "Site oficial");
        verify(settings, times(5)).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void salvarAddsWarningWhenUploadFails() throws IOException {
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                new AdminConfiguracoesGeralController.ConfigGeralForm();
        MultipartFile logo = org.mockito.Mockito.mock(MultipartFile.class);
        when(logo.isEmpty()).thenReturn(false);
        when(logo.getOriginalFilename()).thenReturn("logo.png");
        when(logo.getInputStream()).thenThrow(new IOException("falha no disco"));
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, logo, null, "identidade", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/geral");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("warning");
        Assertions.assertThat((String) attrs.getFlashAttributes().get("warning"))
                .contains("Falha ao salvar logo inicial.");
        Assertions.assertThat(attrs.getFlashAttributes()).doesNotContainKey("success");
        verify(settings, times(10)).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void salvarWithBlankSectionPersistsAllSections() {
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                new AdminConfiguracoesGeralController.ConfigGeralForm();
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, null, null, "   ", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/geral");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Configuracoes gerais atualizadas.");
        verify(settings, times(34)).upsert(anyString(), anyString(), anyString());
        verify(settings).upsert(eq("contato.email"), eq(""), eq("Email principal"));
        verify(settings).upsert(eq("endereco.logradouro"), eq(""), eq("Endereco"));
        verify(settings).upsert(eq("app.estoque.alerta.enabled"), eq("false"), eq("Alerta estoque ativo"));
    }
}
