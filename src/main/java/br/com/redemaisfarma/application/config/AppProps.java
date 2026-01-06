package br.com.redemaisfarma.application.config;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProps {
    private final AppSettingService settings;
    private final Environment environment;

    public AppProps(AppSettingService settings, Environment environment) {
        this.settings = settings;
        this.environment = environment;
    }

    public String getWhatsapp() {
        String envValue = environment.getProperty("app.whatsapp", environment.getProperty("APP_WHATSAPP", ""));
        String fallback = envValue == null ? "" : envValue;
        return settings.get("contato.whatsapp").orElseGet(() -> settings.getOrDefault("whatsapp", fallback));
    }
}
