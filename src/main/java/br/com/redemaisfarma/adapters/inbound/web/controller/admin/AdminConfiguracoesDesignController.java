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
@RequestMapping("/admin/configuracoes/design")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesDesignController {

    private static final String KEY_DARK_DEFAULT = "design.dark_default";
    private static final String KEY_ROUNDED = "design.rounded";
    private static final String KEY_ANIMACOES = "design.animacoes";
    private static final String KEY_FONT_FAMILY = "design.font_family";
    private static final String KEY_RADIUS = "design.radius";
    private static final String KEY_SPACING = "design.spacing";

    private final AppSettingService settings;

    public AdminConfiguracoesDesignController(AppSettingService settings) {
        this.settings = settings;
    }

    @GetMapping
    public String form(Model model) {
        DesignForm form = new DesignForm();
        form.setDarkByDefault(settings.getBoolean(KEY_DARK_DEFAULT, false));
        form.setUsarBordasArredondadas(settings.getBoolean(KEY_ROUNDED, true));
        form.setAnimacoesAtivas(settings.getBoolean(KEY_ANIMACOES, true));
        form.setFontFamily(settings.getOrDefault(KEY_FONT_FAMILY, ""));
        form.setRadius(settings.getOrDefault(KEY_RADIUS, ""));
        form.setSpacing(settings.getOrDefault(KEY_SPACING, ""));
        model.addAttribute("design", form);
        return "pages/admin/configuracoes/design";
    }

    @PostMapping
    public String salvar(@ModelAttribute("design") DesignForm form, RedirectAttributes ra) {
        settings.upsert(KEY_DARK_DEFAULT, bool(form.getDarkByDefault()), "Tema escuro por padrao");
        settings.upsert(KEY_ROUNDED, bool(form.getUsarBordasArredondadas()), "Bordas arredondadas");
        settings.upsert(KEY_ANIMACOES, bool(form.getAnimacoesAtivas()), "Animacoes ativas");
        settings.upsert(KEY_FONT_FAMILY, nullSafe(form.getFontFamily()), "Font family");
        settings.upsert(KEY_RADIUS, nullSafe(form.getRadius()), "Raio de borda");
        settings.upsert(KEY_SPACING, nullSafe(form.getSpacing()), "Base de espacamento");
        ra.addFlashAttribute("success", "Design atualizado.");
        return "redirect:/admin/configuracoes/design";
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String bool(Boolean value) {
        return Boolean.TRUE.equals(value) ? "true" : "false";
    }

    public static class DesignForm {
        private Boolean darkByDefault;
        private Boolean usarBordasArredondadas;
        private Boolean animacoesAtivas;
        private String fontFamily;
        private String radius;
        private String spacing;

        public Boolean getDarkByDefault() {
            return darkByDefault;
        }

        public void setDarkByDefault(Boolean darkByDefault) {
            this.darkByDefault = darkByDefault;
        }

        public Boolean getUsarBordasArredondadas() {
            return usarBordasArredondadas;
        }

        public void setUsarBordasArredondadas(Boolean usarBordasArredondadas) {
            this.usarBordasArredondadas = usarBordasArredondadas;
        }

        public Boolean getAnimacoesAtivas() {
            return animacoesAtivas;
        }

        public void setAnimacoesAtivas(Boolean animacoesAtivas) {
            this.animacoesAtivas = animacoesAtivas;
        }

        public String getFontFamily() {
            return fontFamily;
        }

        public void setFontFamily(String fontFamily) {
            this.fontFamily = fontFamily;
        }

        public String getRadius() {
            return radius;
        }

        public void setRadius(String radius) {
            this.radius = radius;
        }

        public String getSpacing() {
            return spacing;
        }

        public void setSpacing(String spacing) {
            this.spacing = spacing;
        }
    }
}
