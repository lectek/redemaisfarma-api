/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 */
package br.com.redemaisfarma.application.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value={"/admin"})
public class AdminController {
    private static final String VIEW_PREFIX = "pages/admin/";

    private String viewRoot(String page) {
        return VIEW_PREFIX + page;
    }

    private String view2(String section, String page) {
        return VIEW_PREFIX + section + "/" + page;
    }

    private String view3(String section, String sub, String page) {
        return VIEW_PREFIX + section + "/" + sub + "/" + page;
    }

    private static String titleize(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String[] parts = s.split("[-_]");
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) continue;
            b.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            b.append(' ');
        }
        return b.toString().trim();
    }

    private static void setCommon(Model model, String pageTitle, String active) {
        model.addAttribute("pageTitle", (Object)pageTitle);
        model.addAttribute("active", (Object)active);
    }

    @GetMapping(value={"", "/"})
    public String root(Model model) {
        AdminController.setCommon(model, "Dashboard", "dashboard");
        return this.viewRoot("dashboard");
    }

    @GetMapping(value={"/painel"})
    public String redirectPainel() {
        return "redirect:/admin";
    }

    @GetMapping(value={"/{page}"})
    public String pageRoot(@PathVariable String page, Model model) {
        AdminController.setCommon(model, AdminController.titleize(page), page);
        return this.viewRoot(page);
    }

    @GetMapping(value={"/{section}/{page}"})
    public String page2(@PathVariable String section, @PathVariable String page, Model model) {
        AdminController.setCommon(model, AdminController.titleize(page), section);
        return this.view2(section, page);
    }

    @GetMapping(value={"/{section}/{sub}/{page}"})
    public String page3(@PathVariable String section, @PathVariable String sub, @PathVariable String page, Model model) {
        AdminController.setCommon(model, AdminController.titleize(page), section);
        return this.view3(section, sub, page);
    }
}

