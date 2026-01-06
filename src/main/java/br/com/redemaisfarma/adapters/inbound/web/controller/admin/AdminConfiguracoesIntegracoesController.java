package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/integracoes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesIntegracoesController {

    private static final String KEY_API_KEY = "integracoes.api_key";
    private static final String KEY_API_SECRET = "integracoes.api_secret";
    private static final String KEY_WEBHOOK_URL = "integracoes.webhook_url";

    private final AppSettingService settings;

    public AdminConfiguracoesIntegracoesController(AppSettingService settings) {
        this.settings = settings;
    }

    @GetMapping
    public String form(Model model) {
        IntegracoesForm form = new IntegracoesForm();
        form.setApiKey(settings.getOrDefault(KEY_API_KEY, ""));
        form.setApiSecret(settings.getOrDefault(KEY_API_SECRET, ""));
        form.setWebhookUrl(settings.getOrDefault(KEY_WEBHOOK_URL, ""));
        model.addAttribute("cfg", form);
        model.addAttribute("webhookConfigured", form.getWebhookUrl() != null && !form.getWebhookUrl().isBlank());
        return "pages/admin/configuracoes/integracoes";
    }

    @PostMapping
    public String salvar(@ModelAttribute("cfg") IntegracoesForm form, RedirectAttributes ra) {
        settings.upsert(KEY_API_KEY, nullSafe(form.getApiKey()), "API key integracoes");
        settings.upsert(KEY_API_SECRET, nullSafe(form.getApiSecret()), "API secret integracoes");
        settings.upsert(KEY_WEBHOOK_URL, nullSafe(form.getWebhookUrl()), "Webhook integracoes");
        ra.addFlashAttribute("success", "Integracoes atualizadas.");
        return "redirect:/admin/configuracoes/integracoes";
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }

    public static class IntegracoesForm {
        private String apiKey;
        private String apiSecret;
        private String webhookUrl;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }
    }
}
