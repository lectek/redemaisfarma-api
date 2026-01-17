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
public class AdminConfiguracoesBrandingController {

    private static final String KEY_LOGO_URL = "branding.logo_url";
    private static final String KEY_LOGO_URL_DARK = "branding.logo_url_dark";
    private static final String KEY_FAVICON_URL = "branding.favicon_url";
    private static final String KEY_COLOR_PRIMARY = "branding.cor_primaria";
    private static final String KEY_COLOR_SECONDARY = "branding.cor_secundaria";
    private static final String KEY_COLOR_ACCENT = "branding.cor_acento";
    private static final String KEY_HOME_HERO_URL = "branding.home_hero_url";
    private static final String KEY_HOME_HERO_ALT = "branding.home_hero_alt";
    private static final String KEY_BANNER_PROMO_URL = "branding.banner_promocao_url";
    private static final String KEY_FONT_FAMILY = "branding.font_family";
    private static final String KEY_THEME = "branding.tema";
    private static final String KEY_RADIUS = "branding.radius";
    private static final String KEY_LOGO_SIZE = "branding.logo_size";

    private static final String LEGACY_LOGO_URL = "GERAL.logo_inicial_url";
    private static final String LEGACY_FAVICON_URL = "GERAL.favicon_url";
    private static final String LEGACY_HOME_HERO_URL = "GERAL.home_hero_imagem_url";

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

    private final AppSettingService settings;

    public AdminConfiguracoesBrandingController(AppSettingService settings) {
        this.settings = settings;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("branding", loadForm());
        return "pages/admin/configuracoes/branding";
    }

    @PostMapping
    public String salvar(@ModelAttribute("branding") BrandingForm branding,
                         @RequestParam(name = "logoFile", required = false) MultipartFile logoFile,
                         RedirectAttributes ra) {
        boolean uploadFailed = false;
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                branding.setLogoUrl(storeUpload(logoFile, "logo"));
            } catch (IOException ex) {
                uploadFailed = true;
            }
        }

        settings.upsert(KEY_LOGO_URL, nullSafe(branding.getLogoUrl()), "Logo principal");
        settings.upsert(KEY_LOGO_URL_DARK, nullSafe(branding.getLogoUrlDark()), "Logo para fundo escuro");
        settings.upsert(KEY_FAVICON_URL, nullSafe(branding.getFaviconUrl()), "Favicon");
        settings.upsert(KEY_COLOR_PRIMARY, nullSafe(branding.getCorPrimaria()), "Cor primaria");
        settings.upsert(KEY_COLOR_SECONDARY, nullSafe(branding.getCorSecundaria()), "Cor secundaria");
        settings.upsert(KEY_COLOR_ACCENT, nullSafe(branding.getCorAcento()), "Cor acento");
        settings.upsert(KEY_HOME_HERO_URL, nullSafe(branding.getHomeHeroUrl()), "Imagem principal da home");
        settings.upsert(KEY_HOME_HERO_ALT, nullSafe(branding.getHomeHeroAlt()), "Texto alternativo do hero");
        settings.upsert(KEY_BANNER_PROMO_URL, nullSafe(branding.getBannerPromocao()), "Banner promocao");
        settings.upsert(KEY_FONT_FAMILY, nullSafe(branding.getFontFamily()), "Font-family");
        settings.upsert(KEY_THEME, nullSafe(branding.getTema()), "Tema");
        settings.upsert(KEY_RADIUS, nullSafe(branding.getRadius()), "Raio de borda");
        settings.upsert(KEY_LOGO_SIZE, nullSafe(branding.getLogoSize()), "Tamanho da logo");

        if (uploadFailed) {
            ra.addFlashAttribute("warning", "Alguns uploads falharam; verifique os arquivos enviados.");
        } else {
            ra.addFlashAttribute("success", "Branding atualizado.");
        }
        return "redirect:/admin/configuracoes/branding";
    }

    private BrandingForm loadForm() {
        Map<String, String> cfg = settings.getAllByKeys(ALL_KEYS);
        BrandingForm form = new BrandingForm();
        form.setLogoUrl(firstValue(cfg, KEY_LOGO_URL, LEGACY_LOGO_URL));
        form.setLogoUrlDark(cfg.getOrDefault(KEY_LOGO_URL_DARK, ""));
        form.setFaviconUrl(firstValue(cfg, KEY_FAVICON_URL, LEGACY_FAVICON_URL));
        form.setCorPrimaria(cfg.getOrDefault(KEY_COLOR_PRIMARY, ""));
        form.setCorSecundaria(cfg.getOrDefault(KEY_COLOR_SECONDARY, ""));
        form.setCorAcento(cfg.getOrDefault(KEY_COLOR_ACCENT, ""));
        form.setHomeHeroUrl(firstValue(cfg, KEY_HOME_HERO_URL, LEGACY_HOME_HERO_URL));
        form.setHomeHeroAlt(cfg.getOrDefault(KEY_HOME_HERO_ALT, ""));
        form.setBannerPromocao(cfg.getOrDefault(KEY_BANNER_PROMO_URL, ""));
        form.setFontFamily(cfg.getOrDefault(KEY_FONT_FAMILY, ""));
        form.setTema(cfg.getOrDefault(KEY_THEME, "auto"));
        form.setRadius(cfg.getOrDefault(KEY_RADIUS, ""));
        form.setLogoSize(cfg.getOrDefault(KEY_LOGO_SIZE, "medium"));
        return form;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private static String firstValue(Map<String, String> cfg, String primary, String fallback) {
        String value = cfg.get(primary);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return cfg.getOrDefault(fallback, "");
    }

    private static String storeUpload(MultipartFile file, String prefix) throws IOException {
        String original = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "upload"));
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > -1 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }
        String filename = prefix + "-" + System.currentTimeMillis() + ext;
        Path dir = Paths.get("media", "branding");
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/media/branding/" + filename;
    }

    public static class BrandingForm {
        private String logoUrl;
        private String logoUrlDark;
        private String faviconUrl;
        private String corPrimaria;
        private String corSecundaria;
        private String corAcento;
        private String homeHeroUrl;
        private String homeHeroAlt;
        private String bannerPromocao;
        private String fontFamily;
        private String tema;
        private String radius;
        private String logoSize;

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public String getLogoUrlDark() {
            return logoUrlDark;
        }

        public void setLogoUrlDark(String logoUrlDark) {
            this.logoUrlDark = logoUrlDark;
        }

        public String getFaviconUrl() {
            return faviconUrl;
        }

        public void setFaviconUrl(String faviconUrl) {
            this.faviconUrl = faviconUrl;
        }

        public String getCorPrimaria() {
            return corPrimaria;
        }

        public void setCorPrimaria(String corPrimaria) {
            this.corPrimaria = corPrimaria;
        }

        public String getCorSecundaria() {
            return corSecundaria;
        }

        public void setCorSecundaria(String corSecundaria) {
            this.corSecundaria = corSecundaria;
        }

        public String getCorAcento() {
            return corAcento;
        }

        public void setCorAcento(String corAcento) {
            this.corAcento = corAcento;
        }

        public String getHomeHeroUrl() {
            return homeHeroUrl;
        }

        public void setHomeHeroUrl(String homeHeroUrl) {
            this.homeHeroUrl = homeHeroUrl;
        }

        public String getHomeHeroAlt() {
            return homeHeroAlt;
        }

        public void setHomeHeroAlt(String homeHeroAlt) {
            this.homeHeroAlt = homeHeroAlt;
        }

        public String getBannerPromocao() {
            return bannerPromocao;
        }

        public void setBannerPromocao(String bannerPromocao) {
            this.bannerPromocao = bannerPromocao;
        }

        public String getFontFamily() {
            return fontFamily;
        }

        public void setFontFamily(String fontFamily) {
            this.fontFamily = fontFamily;
        }

        public String getTema() {
            return tema;
        }

        public void setTema(String tema) {
            this.tema = tema;
        }

        public String getRadius() {
            return radius;
        }

        public void setRadius(String radius) {
            this.radius = radius;
        }

        public String getLogoSize() {
            return logoSize;
        }

        public void setLogoSize(String logoSize) {
            this.logoSize = logoSize;
        }
    }
}
