package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/branding")
@PreAuthorize("hasRole('ADMIN')")
public final class AdminConfiguracoesBrandingController {

    /**
     * Branding key for default logo URL.
     */
    private static final String KEY_LOGO_URL = "branding.logo_url";

    /**
     * Branding key for dark logo URL.
     */
    private static final String KEY_LOGO_URL_DARK = "branding.logo_url_dark";

    /**
     * Branding key for favicon URL.
     */
    private static final String KEY_FAVICON_URL = "branding.favicon_url";

    /**
     * Branding key for primary color.
     */
    private static final String KEY_COLOR_PRIMARY = "branding.cor_primaria";

    /**
     * Branding key for secondary color.
     */
    private static final String KEY_COLOR_SECONDARY = "branding.cor_secundaria";

    /**
     * Branding key for accent color.
     */
    private static final String KEY_COLOR_ACCENT = "branding.cor_acento";

    /**
     * Branding key for home hero image URL.
     */
    private static final String KEY_HOME_HERO_URL = "branding.home_hero_url";

    /**
     * Branding key for home hero alt text.
     */
    private static final String KEY_HOME_HERO_ALT = "branding.home_hero_alt";

    /**
     * Branding key for promo banner URL.
     */
    private static final String KEY_BANNER_PROMO_URL =
            "branding.banner_promocao_url";

    /**
     * Branding key for font family.
     */
    private static final String KEY_FONT_FAMILY = "branding.font_family";

    /**
     * Branding key for theme.
     */
    private static final String KEY_THEME = "branding.tema";

    /**
     * Branding key for border radius.
     */
    private static final String KEY_RADIUS = "branding.radius";

    /**
     * Branding key for logo size.
     */
    private static final String KEY_LOGO_SIZE = "branding.logo_size";

    /**
     * Legacy key for logo URL.
     */
    private static final String LEGACY_LOGO_URL = "GERAL.logo_inicial_url";

    /**
     * Legacy key for favicon URL.
     */
    private static final String LEGACY_FAVICON_URL = "GERAL.favicon_url";

    /**
     * Legacy key for home hero image URL.
     */
    private static final String LEGACY_HOME_HERO_URL =
            "GERAL.home_hero_imagem_url";

    /**
     * All keys loaded from settings store.
     */
    private static final Set<String> ALL_KEYS = Set.of(
            KEY_LOGO_URL,
            KEY_LOGO_URL_DARK,
            KEY_FAVICON_URL,
            KEY_COLOR_PRIMARY,
            KEY_COLOR_SECONDARY,
            KEY_COLOR_ACCENT,
            KEY_HOME_HERO_URL,
            KEY_HOME_HERO_ALT,
            KEY_BANNER_PROMO_URL,
            KEY_FONT_FAMILY,
            KEY_THEME,
            KEY_RADIUS,
            KEY_LOGO_SIZE,
            LEGACY_LOGO_URL,
            LEGACY_FAVICON_URL,
            LEGACY_HOME_HERO_URL
    );

    /**
     * Service used to read and write settings.
     */
    private final AppSettingService settings;

    /**
     * Creates controller with settings dependency.
     *
     * @param appSettingService settings service
     */
    public AdminConfiguracoesBrandingController(
            final AppSettingService appSettingService
    ) {
        this.settings = appSettingService;
    }

    /**
     * Renders branding configuration form.
     *
     * @param model view model
     * @return branding page
     */
    @GetMapping
    public String form(final Model model) {
        model.addAttribute("branding", loadForm());
        return "pages/admin/configuracoes/branding";
    }

    /**
     * Persists branding settings and optional uploaded logo.
     *
     * @param branding branding payload
     * @param logoFile optional logo file
     * @param ra redirect attributes
     * @return redirect to branding page
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("branding") final BrandingForm branding,
            @RequestParam(name = "logoFile", required = false)
            final MultipartFile logoFile,
            final RedirectAttributes ra
    ) {
        boolean uploadFailed = false;
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                branding.setLogoUrl(storeUpload(logoFile, "logo"));
            } catch (IOException ex) {
                uploadFailed = true;
            }
        }

        saveSetting(KEY_LOGO_URL, branding.getLogoUrl(), "Logo principal");
        saveSetting(
                KEY_LOGO_URL_DARK,
                branding.getLogoUrlDark(),
                "Logo para fundo escuro"
        );
        saveSetting(KEY_FAVICON_URL, branding.getFaviconUrl(), "Favicon");
        saveSetting(
                KEY_COLOR_PRIMARY,
                branding.getCorPrimaria(),
                "Cor primaria"
        );
        saveSetting(
                KEY_COLOR_SECONDARY,
                branding.getCorSecundaria(),
                "Cor secundaria"
        );
        saveSetting(KEY_COLOR_ACCENT, branding.getCorAcento(), "Cor acento");
        saveSetting(
                KEY_HOME_HERO_URL,
                branding.getHomeHeroUrl(),
                "Imagem principal da home"
        );
        saveSetting(
                KEY_HOME_HERO_ALT,
                branding.getHomeHeroAlt(),
                "Texto alternativo do hero"
        );
        saveSetting(
                KEY_BANNER_PROMO_URL,
                branding.getBannerPromocao(),
                "Banner promocao"
        );
        saveSetting(KEY_FONT_FAMILY, branding.getFontFamily(), "Font-family");
        saveSetting(KEY_THEME, branding.getTema(), "Tema");
        saveSetting(KEY_RADIUS, branding.getRadius(), "Raio de borda");
        saveSetting(KEY_LOGO_SIZE, branding.getLogoSize(), "Tamanho da logo");

        if (uploadFailed) {
            ra.addFlashAttribute(
                    "warning",
                    "Alguns uploads falharam; verifique os arquivos enviados."
            );
        } else {
            ra.addFlashAttribute("success", "Branding atualizado.");
        }
        return "redirect:/admin/configuracoes/branding";
    }

    /**
     * Saves one setting with null-safe value.
     *
     * @param key setting key
     * @param value setting value
     * @param description setting description
     */
    private void saveSetting(
            final String key,
            final String value,
            final String description
    ) {
        settings.upsert(key, nullSafe(value), description);
    }

    /**
     * Loads branding form values from current settings.
     *
     * @return populated form
     */
    private BrandingForm loadForm() {
        final Map<String, String> cfg = settings.getAllByKeys(ALL_KEYS);
        final BrandingForm form = new BrandingForm();
        form.setLogoUrl(firstValue(cfg, KEY_LOGO_URL, LEGACY_LOGO_URL));
        form.setLogoUrlDark(cfg.getOrDefault(KEY_LOGO_URL_DARK, ""));
        form.setFaviconUrl(
                firstValue(cfg, KEY_FAVICON_URL, LEGACY_FAVICON_URL)
        );
        form.setCorPrimaria(cfg.getOrDefault(KEY_COLOR_PRIMARY, ""));
        form.setCorSecundaria(cfg.getOrDefault(KEY_COLOR_SECONDARY, ""));
        form.setCorAcento(cfg.getOrDefault(KEY_COLOR_ACCENT, ""));
        form.setHomeHeroUrl(
                firstValue(cfg, KEY_HOME_HERO_URL, LEGACY_HOME_HERO_URL)
        );
        form.setHomeHeroAlt(cfg.getOrDefault(KEY_HOME_HERO_ALT, ""));
        form.setBannerPromocao(cfg.getOrDefault(KEY_BANNER_PROMO_URL, ""));
        form.setFontFamily(cfg.getOrDefault(KEY_FONT_FAMILY, ""));
        form.setTema(cfg.getOrDefault(KEY_THEME, "auto"));
        form.setRadius(cfg.getOrDefault(KEY_RADIUS, ""));
        form.setLogoSize(cfg.getOrDefault(KEY_LOGO_SIZE, "medium"));
        return form;
    }

    /**
     * Returns empty string when value is null.
     *
     * @param value source value
     * @return null-safe value
     */
    private static String nullSafe(final String value) {
        return value == null ? "" : value;
    }

    /**
     * Gets primary value or fallback key when primary is blank.
     *
     * @param cfg settings map
     * @param primary primary key
     * @param fallback fallback key
     * @return resolved value
     */
    private static String firstValue(
            final Map<String, String> cfg,
            final String primary,
            final String fallback
    ) {
        final String value = cfg.get(primary);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return cfg.getOrDefault(fallback, "");
    }

    /**
     * Stores uploaded branding file and returns public path.
     *
     * @param file uploaded file
     * @param prefix filename prefix
     * @return public file URL
     * @throws IOException when upload fails
     */
    private static String storeUpload(
            final MultipartFile file,
            final String prefix
    ) throws IOException {
        final String original = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), "upload")
        );
        String ext = "";
        final int dot = original.lastIndexOf('.');
        if (dot > -1 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }
        final String filename = prefix + "-" + System.currentTimeMillis() + ext;
        final Path dir = Paths.get("media", "branding");
        Files.createDirectories(dir);
        final Path target = dir.resolve(filename);
        Files.copy(
                file.getInputStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
        return "/media/branding/" + filename;
    }

    /**
     * Form payload for branding settings page.
     */
    @Getter
    @Setter
    public static final class BrandingForm {

        /**
         * Main logo URL.
         */
        private String logoUrl;

        /**
         * Dark mode logo URL.
         */
        private String logoUrlDark;

        /**
         * Favicon URL.
         */
        private String faviconUrl;

        /**
         * Primary color.
         */
        private String corPrimaria;

        /**
         * Secondary color.
         */
        private String corSecundaria;

        /**
         * Accent color.
         */
        private String corAcento;

        /**
         * Home hero image URL.
         */
        private String homeHeroUrl;

        /**
         * Home hero alt text.
         */
        private String homeHeroAlt;

        /**
         * Promo banner URL.
         */
        private String bannerPromocao;

        /**
         * Font family.
         */
        private String fontFamily;

        /**
         * Theme option.
         */
        private String tema;

        /**
         * Border radius option.
         */
        private String radius;

        /**
         * Logo size option.
         */
        private String logoSize;
    }
}
