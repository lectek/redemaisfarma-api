/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AppSettingEntity;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value={"/admin/settings"})
@PreAuthorize(value="hasRole('ADMIN')")
@Validated
public class AdminSettingsController {
    private final AppSettingService service;

    public AdminSettingsController(AppSettingService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(value="q", required=false) String q, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value="size", defaultValue="20") int size, Model model) {
        PageRequest pageable = PageRequest.of((int)Math.max(page, 0), (int)Math.max(size, 1));
        Page<AppSettingEntity> pageData = this.service.list(q, (Pageable)pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("q", (Object)(q == null ? "" : q));
        model.addAttribute("size", (Object)size);
        return "pages/admin/settings";
    }

    @GetMapping(value={"/new"})
    public String newForm(Model model) {
        model.addAttribute("form", (Object)new Form());
        return "pages/admin/settings-form";
    }

    @GetMapping(value={"/edit/{id}"})
    public String editForm(@PathVariable Long id, Model model) {
        AppSettingEntity entity = this.service.findById(id).orElseThrow(() -> new IllegalArgumentException("Config n\u00e3o encontrada"));
        Form form = Form.from(entity);
        model.addAttribute("form", (Object)form);
        return "pages/admin/settings-form";
    }

    @PostMapping
    public String create(@ModelAttribute @Validated Form form, RedirectAttributes ra) {
        this.service.create(form.getSettingKey().trim(), AdminSettingsController.nullSafe(form.getSettingValue()), AdminSettingsController.nullSafe(form.getDescription()));
        ra.addFlashAttribute("success", (Object)"Configura\u00e7\u00e3o criada com sucesso.");
        return "redirect:/admin/settings";
    }

    @PostMapping(value={"/{id}"})
    public String update(@PathVariable Long id, @ModelAttribute @Validated Form form, RedirectAttributes ra) {
        this.service.update(id, form.getSettingKey().trim(), AdminSettingsController.nullSafe(form.getSettingValue()), AdminSettingsController.nullSafe(form.getDescription()));
        ra.addFlashAttribute("success", (Object)"Configura\u00e7\u00e3o atualizada.");
        return "redirect:/admin/settings";
    }

    @PostMapping(value={"/delete/{id}"})
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        this.service.delete(id);
        ra.addFlashAttribute("success", (Object)"Configura\u00e7\u00e3o removida.");
        return "redirect:/admin/settings";
    }

    private static String nullSafe(String v) {
        return v == null ? "" : v;
    }

    public static class Form {
        private Long id;
        @NotBlank(message="Chave \u00e9 obrigat\u00f3ria")
        private @NotBlank(message="Chave \u00e9 obrigat\u00f3ria") String settingKey;
        private String settingValue;
        private String description;

        public static Form from(AppSettingEntity e) {
            Form f = new Form();
            f.setId(e.getId());
            f.setSettingKey(e.getSettingKey());
            f.setSettingValue(e.getSettingValue());
            f.setDescription(e.getDescription());
            return f;
        }

        public Long getId() {
            return this.id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSettingKey() {
            return this.settingKey;
        }

        public void setSettingKey(String settingKey) {
            this.settingKey = settingKey;
        }

        public String getSettingValue() {
            return this.settingValue;
        }

        public void setSettingValue(String settingValue) {
            this.settingValue = settingValue;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
