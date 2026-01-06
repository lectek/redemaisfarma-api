package br.com.redemaisfarma.adapters.inbound.web.advice;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class BrandingModelAdvice {

    private static final String KEY_LOGO_URL = "branding.logo_url";
    private static final String KEY_FAVICON_URL = "branding.favicon_url";
    private static final String KEY_HOME_HERO_URL = "branding.home_hero_url";
    private static final String KEY_HOME_HERO_TEXTO = "branding.home_hero_texto";
    private static final String LEGACY_LOGO_URL = "GERAL.logo_inicial_url";
    private static final String LEGACY_FAVICON_URL = "GERAL.favicon_url";
    private static final String LEGACY_HOME_HERO_URL = "GERAL.home_hero_imagem_url";
    private static final String LEGACY_HOME_HERO_TEXTO = "GERAL.home_hero_texto";

    private static final String FALLBACK_LOGO = "/images/logofarma.jpg";
    private static final String FALLBACK_FAVICON = "/images/favicon.png";

    private final AppSettingService settings;

    public BrandingModelAdvice(AppSettingService settings) {
        this.settings = settings;
    }

    @ModelAttribute
    public void addBranding(Model model) {
        String logoUrl = firstNonBlank(
                settings.get(KEY_LOGO_URL).orElse(""),
                settings.get(LEGACY_LOGO_URL).orElse(""),
                FALLBACK_LOGO
        );
        String faviconUrl = firstNonBlank(
                settings.get(KEY_FAVICON_URL).orElse(""),
                settings.get(LEGACY_FAVICON_URL).orElse(""),
                FALLBACK_FAVICON
        );
        String heroUrl = firstNonBlank(
                settings.get(KEY_HOME_HERO_URL).orElse(""),
                settings.get(LEGACY_HOME_HERO_URL).orElse(""),
                ""
        );
        String heroTexto = firstNonBlank(
                settings.get(KEY_HOME_HERO_TEXTO).orElse(""),
                settings.get(LEGACY_HOME_HERO_TEXTO).orElse(""),
                ""
        );
        model.addAttribute("brandingLogoUrl", logoUrl);
        model.addAttribute("brandingFaviconUrl", faviconUrl);
        model.addAttribute("brandingHomeHeroUrl", heroUrl);
        model.addAttribute("brandingHomeHeroTexto", heroTexto);
    }

    private static String firstNonBlank(String primary, String fallback, String defaultValue) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return defaultValue;
    }
}
