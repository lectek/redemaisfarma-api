// src/main/java/br/com/redemaisfarma/adapters/inbound/web/LandingController.java
package br.com.redemaisfarma.adapters.inbound.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    // Tela de login do cliente (home pública)
    @GetMapping("/auth/login")
    public String loginCliente() {
        return "pages/auth/login"; // templates/pages/auth/login.html
    }

    // ⚠️ Admin é app separado → nenhuma rota /admin no site
}
