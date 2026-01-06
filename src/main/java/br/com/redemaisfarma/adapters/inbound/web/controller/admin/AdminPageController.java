// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/admin/AdminPageController.java
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@Profile("legacy")
@RequestMapping("/admin")
public class AdminPageController {

    // index do admin (mantem /admin/index para evitar conflito com redirect)
    @GetMapping("/index")
    public String adminIndex() {
        return "pages/admin/index";
    }

    // 1 nível: /admin/produtos -> pages/admin/produtos/index.html (se quiser)
    @GetMapping("/{sec}")
    public String adminSection(@PathVariable String sec) {
        return "pages/admin/" + sec + "/index";
    }

    // 2 níveis: /admin/produtos/lista -> pages/admin/produtos/lista.html
    @GetMapping("/{sec}/{page}")
    public String adminPage(@PathVariable String sec, @PathVariable String page) {
        return "pages/admin/" + sec + "/" + page;
    }

    // 3 níveis (opcional): /admin/configuracoes/email/central -> pages/admin/configuracoes/email/central.html
    @GetMapping("/{sec}/{sub}/{page}")
    public String adminDeepPage(@PathVariable String sec, @PathVariable String sub, @PathVariable String page) {
        return "pages/admin/" + sec + "/" + sub + "/" + page;
    }
}
