package br.com.redemaisfarma.adapters.inbound.web.advice;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.CustomerEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.CustomerRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Component
@ControllerAdvice
public class BrandingModelAdvice {

    private static final Logger log = LoggerFactory.getLogger(BrandingModelAdvice.class);

    private static final String KEY_LOGO_URL = "branding.logo_url";
    private static final String KEY_FAVICON_URL = "branding.favicon_url";
    private static final String KEY_HOME_HERO_URL = "branding.home_hero_url";
    private static final String KEY_HOME_HERO_TEXTO = "branding.home_hero_texto";
    private static final String KEY_HOME_HERO_VIDEO_URL = "branding.home_hero_video_url";
    private static final String KEY_LOGO_SIZE = "branding.logo_size";
    private static final String LEGACY_LOGO_URL = "GERAL.logo_inicial_url";
    private static final String LEGACY_FAVICON_URL = "GERAL.favicon_url";
    private static final String LEGACY_HOME_HERO_URL = "GERAL.home_hero_imagem_url";
    private static final String LEGACY_HOME_HERO_TEXTO = "GERAL.home_hero_texto";

    private static final String FALLBACK_LOGO = "/images/logonova.png";
    private static final String FALLBACK_FAVICON = "/images/favicon.png";
    private static final String FALLBACK_HOME_HERO = "/images/absorventepronto.png";

    private final AppSettingService settings;
    private final ResourceLoader resourceLoader;
    private final UsuarioRepository usuarios;
    private final CustomerRepository customers;

    public BrandingModelAdvice(AppSettingService settings,
                               ResourceLoader resourceLoader,
                               UsuarioRepository usuarios,
                               CustomerRepository customers) {
        this.settings = settings;
        this.resourceLoader = resourceLoader;
        this.usuarios = usuarios;
        this.customers = customers;
    }

    @ModelAttribute
    public void addBranding(HttpServletRequest request, Model model) {
        String uri = request != null ? request.getRequestURI() : null;
        if (uri != null && shouldSkipBranding(uri)) {
            return;
        }
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
        String heroSetting = firstNonBlank(
                settings.get(KEY_HOME_HERO_URL).orElse(""),
                settings.get(LEGACY_HOME_HERO_URL).orElse(""),
                ""
        );
        String heroUrl = resolveHeroUrl(heroSetting);
        String heroTexto = firstNonBlank(
                settings.get(KEY_HOME_HERO_TEXTO).orElse(""),
                settings.get(LEGACY_HOME_HERO_TEXTO).orElse(""),
                ""
        );

        Resource fallback = resourceLoader.getResource("classpath:static" + FALLBACK_HOME_HERO);
        boolean fallbackAvailable = fallback.exists();
        boolean fallbackInUse = FALLBACK_HOME_HERO.equals(heroUrl) && !looksLikeImage(heroSetting);

        model.addAttribute("brandingLogoUrl", logoUrl);
        model.addAttribute("brandingFaviconUrl", faviconUrl);
        model.addAttribute("brandingHomeHeroUrl", heroSetting);
        model.addAttribute("brandingHomeHeroImageUrl", heroUrl);
        String heroVideoSetting = settings.get(KEY_HOME_HERO_VIDEO_URL).orElse("");
        String heroVideoUrl = looksLikeVideo(heroVideoSetting) ? heroVideoSetting : "";

        String heroVideoType = detectVideoMimeType(heroVideoUrl);
        model.addAttribute("brandingHomeHeroTexto", heroTexto);
        model.addAttribute("brandingHomeHeroVideoUrl", heroVideoUrl);
        model.addAttribute("brandingHomeHeroVideoPoster", heroUrl);
        model.addAttribute("brandingHomeHeroVideoType", heroVideoType);
        model.addAttribute("brandingLogoSize", parseLogoSize(settings.get(KEY_LOGO_SIZE).orElse("medium")));
        model.addAttribute("brandingHeroPublic", true);
        model.addAttribute("brandingHeroFallbackUsed", fallbackInUse);
        model.addAttribute("brandingHeroFallbackAvailable", fallbackAvailable);
        addHeaderUser(request, model);

        log.debug("Hero available publicly (logged-out) {} (fallbackUsed={}, fileExists={}, heroSetting={})",
                heroUrl, fallbackInUse, fallbackAvailable, heroSetting);
    }

    private void addHeaderUser(HttpServletRequest request, Model model) {
        String identity = request != null && request.getUserPrincipal() != null
                ? nonBlank(request.getUserPrincipal().getName())
                : "";
        String authEmail = extractEmail(request);
        if (identity.isBlank()) {
            model.addAttribute("headerUserAuthenticated", false);
            model.addAttribute("headerUserName", "");
            model.addAttribute("headerUserAvatarUrl", "");
            model.addAttribute("headerUserInitial", "U");
            return;
        }

        model.addAttribute("headerUserAuthenticated", true);
        UsuarioEntity usuario = usuarios.findByEmailOrCpf(identity).orElse(null);
        if (usuario == null && !authEmail.isBlank()) {
            usuario = usuarios.findByEmailIgnoreCase(authEmail).orElse(null);
        }
        if (usuario != null) {
            String displayName = firstNonBlank(usuario.getNome(), usuario.getEmail(), identity);
            model.addAttribute("headerUserName", displayName);
            model.addAttribute("headerUserAvatarUrl", nonBlank(usuario.getAvatarUrl()));
            model.addAttribute("headerUserInitial", firstLetter(displayName));
            return;
        }

        String customerEmail = !authEmail.isBlank() ? authEmail : (identity.contains("@") ? identity.toLowerCase() : "");
        if (!customerEmail.isBlank()) {
            CustomerEntity customer = customers.findByEmail(customerEmail).orElse(null);
            if (customer != null) {
                String displayName = firstNonBlank(customer.getNome(), customer.getEmail(), customerEmail);
                model.addAttribute("headerUserName", displayName);
                model.addAttribute("headerUserAvatarUrl", nonBlank(customer.getAvatarUrl()));
                model.addAttribute("headerUserInitial", firstLetter(displayName));
                return;
            }
        }

        model.addAttribute("headerUserName", identity);
        model.addAttribute("headerUserAvatarUrl", "");
        model.addAttribute("headerUserInitial", firstLetter(identity));
    }

    private String extractEmail(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return "";
        }
        String name = nonBlank(principal.getName());
        if (name.contains("@")) {
            return name.toLowerCase();
        }
        if (principal instanceof Authentication authentication) {
            Object authPrincipal = authentication.getPrincipal();
            if (authPrincipal instanceof UserDetails userDetails) {
                String username = nonBlank(userDetails.getUsername());
                if (username.contains("@")) {
                    return username.toLowerCase();
                }
            }
            if (authPrincipal instanceof OAuth2User oauth2User) {
                Object email = oauth2User.getAttributes().get("email");
                String emailText = email == null ? "" : nonBlank(email.toString());
                if (emailText.contains("@")) {
                    return emailText.toLowerCase();
                }
            }
        }
        return "";
    }

    private static boolean shouldSkipBranding(String uri) {
        return uri.startsWith("/actuator")
                || uri.startsWith("/api/")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/docs");
    }

    private String resolveHeroUrl(String heroSetting) {
        if (heroSetting == null || heroSetting.isBlank()) {
            return FALLBACK_HOME_HERO;
        }
        if (looksLikeImage(heroSetting)) {
            return heroSetting;
        }
        return FALLBACK_HOME_HERO;
    }

    private static boolean looksLikeImage(String url) {
        if (url == null) {
            return false;
        }
        return url.matches("(?i).*\\.(png|jpg|jpeg|webp|gif|svg|avif)(\\?.*)?$");
    }

    private static boolean looksLikeVideo(String url) {
        if (url == null) {
            return false;
        }
        return url.matches("(?i).*\\.(mp4|mov|webm|ogg|m4v)(\\?.*)?$");
    }

    private static String detectVideoMimeType(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String normalized = url.toLowerCase();
        if (normalized.matches(".*\\.(mp4)(\\?.*)?$")) {
            return "video/mp4";
        }
        if (normalized.matches(".*\\.(webm)(\\?.*)?$")) {
            return "video/webm";
        }
        if (normalized.matches(".*\\.(mov|m4v)(\\?.*)?$")) {
            return "video/quicktime";
        }
        if (normalized.matches(".*\\.(ogg)(\\?.*)?$")) {
            return "video/ogg";
        }
        return "";
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

    private static String nonBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstLetter(String value) {
        String normalized = nonBlank(value);
        if (normalized.isBlank()) {
            return "U";
        }
        return normalized.substring(0, 1).toUpperCase();
    }

    private static String parseLogoSize(String raw) {
        if (raw == null) {
            return "medium";
        }
        String normalized = raw.trim().toLowerCase();
        return switch (normalized) {
            case "small", "compact" -> "small";
            case "large", "expanded" -> "large";
            default -> "medium";
        };
    }
}
