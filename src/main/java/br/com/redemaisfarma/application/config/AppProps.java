package br.com.redemaisfarma.application.config;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProps {
    private static final String DEFAULT_WHATSAPP_NUMBER = "5583988853265";
    private static final String DEFAULT_WHATSAPP_DISPLAY = "(83) 98885-3265";

    private final AppSettingService settings;
    private final Environment environment;

    public AppProps(AppSettingService settings, Environment environment) {
        this.settings = settings;
        this.environment = environment;
    }

    public String getWhatsapp() {
        String raw = resolveWhatsappRaw();
        return (raw == null || raw.isBlank()) ? DEFAULT_WHATSAPP_DISPLAY : raw;
    }

    private String sanitizeDigits(String value) {
        if (value == null) return "";
        return value.replaceAll("\\D+", "");
    }

    private String resolveWhatsappRaw() {
        String envValue = environment.getProperty("app.whatsapp", environment.getProperty("APP_WHATSAPP", ""));
        String fallback = envValue == null ? "" : envValue;
        return settings.get("contato.whatsapp").orElseGet(() -> settings.getOrDefault("whatsapp", fallback));
    }

    public String getWhatsappLink() {
        String digits = sanitizeDigits(resolveWhatsappRaw());
        if (digits.isBlank()) digits = DEFAULT_WHATSAPP_NUMBER;
        return "https://wa.me/" + digits;
    }

    public String getWhatsappDigits() {
        String digits = sanitizeDigits(resolveWhatsappRaw());
        return digits.isBlank() ? DEFAULT_WHATSAPP_NUMBER : digits;
    }

    public String getWhatsappDisplay() {
        String raw = resolveWhatsappRaw();
        return (raw == null || raw.isBlank()) ? DEFAULT_WHATSAPP_DISPLAY : raw;
    }

    public String getWhatsappTelephone() {
        String digits = getWhatsappDigits();
        if (!digits.startsWith("55")) digits = "55" + digits;
        return "+" + digits;
    }
}
