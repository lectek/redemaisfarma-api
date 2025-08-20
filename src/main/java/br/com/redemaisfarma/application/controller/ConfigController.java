package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.config.AppConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsável por exibir configurações do sistema de forma limpa.
 */
@Controller
@RequestMapping("/config")
public class ConfigController {

    private final AppConfigProperties appConfig;
    private final Environment environment;

    private final List<ConfigItem> baseItems = new ArrayList<>();
    private LocalDateTime lastRefreshTime;

    @Autowired
    public ConfigController(AppConfigProperties appConfig, Environment environment, MessageSource messages) {
        this.appConfig = appConfig;
        this.environment = environment;

        buildBaseItems();
    }

    @GetMapping
    public String exibirConfiguracoes(Model model) {
        model.addAttribute("configList", baseItems);
        model.addAttribute("lastRefreshTime", lastRefreshTime);
        return "fragments/config :: config";
    }

    private void buildBaseItems() {
        baseItems.clear();
        baseItems.add(new ConfigItem("Perfil do ambiente", appConfig.getEnvProfileLabel()));
        baseItems.add(new ConfigItem("Porta da rede", appConfig.getNetworkPortLabel()));
        baseItems.add(new ConfigItem("Java versão", System.getProperty("java.version")));
        baseItems.add(new ConfigItem("Spring perfil ativo", String.join(", ", environment.getActiveProfiles())));

        lastRefreshTime = LocalDateTime.now();
    }

    /**
     * Classe interna simples representando um item de configuração.
     */
    public static class ConfigItem {
        private final String label;
        private final String value;

        public ConfigItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }
}
