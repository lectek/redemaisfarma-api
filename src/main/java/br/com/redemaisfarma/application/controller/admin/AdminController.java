// src/main/java/br/com/redemaisfarma/application/controller/admin/AdminController.java
package br.com.redemaisfarma.application.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String VIEW_PREFIX = "pages/admin/";

    private String viewRoot(String page) {
        return VIEW_PREFIX + page; // admin/{page}.html
    }
    private String view2(String section, String page) {
        return VIEW_PREFIX + section + "/" + page; // admin/{section}/{page}.html
    }
    private String view3(String section, String sub, String page) {
        return VIEW_PREFIX + section + "/" + sub + "/" + page; // admin/{section}/{sub}/{page}.html
    }

    // Helpers
    private static String titleize(String s) {
        if (s == null || s.isBlank()) return "";
        // "regras-desconto" -> "Regras Desconto"
        String[] parts = s.split("[-_]");
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) continue;
            b.append(Character.toUpperCase(p.charAt(0)))
             .append(p.substring(1));
            b.append(' ');
        }
        return b.toString().trim();
    }
    private static void setCommon(Model model, String pageTitle, String active) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("active", active); // use no header/nav para marcar menu
    }

    /* =========================
       Raiz do Admin
       ========================= */
    @GetMapping({"", "/"})
    public String root(Model model) {
        // escolha "dashboard" como landing (troque para "index" se preferir)
        setCommon(model, "Dashboard", "dashboard");
        return viewRoot("dashboard");
    }

    // Redirects de compatibilidade
    @GetMapping("/painel")
    public String redirectPainel() { return "redirect:/admin"; }

    /* =========================
       1) Páginas na raiz: /admin/{page} -> admin/{page}.html
       Ex.: /admin/design  => templates/pages/admin/design.html
       ========================= */
    @GetMapping("/{page}")
    public String pageRoot(@PathVariable String page, Model model) {
        setCommon(model, titleize(page), page);
        return viewRoot(page);
    }

    /* =========================
       2) Dois níveis: /admin/{section}/{page}
       Ex.: /admin/produtos/lista => templates/pages/admin/produtos/lista.html
       ========================= */
    @GetMapping("/{section}/{page}")
    public String page2(@PathVariable String section,
                        @PathVariable String page,
                        Model model) {
        setCommon(model, titleize(page), section);
        return view2(section, page);
    }

    /* =========================
       3) Três níveis: /admin/{section}/{sub}/{page}
       Ex.: /admin/marketing/emails/campanhas =>
            templates/pages/admin/marketing/emails/campanhas.html
       ========================= */
    @GetMapping("/{section}/{sub}/{page}")
    public String page3(@PathVariable String section,
                        @PathVariable String sub,
                        @PathVariable String page,
                        Model model) {
        // active fica no 1º nível (section), que costuma ser o item do menu lateral
        setCommon(model, titleize(page), section);
        return view3(section, sub, page);
    }
}
