// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/auth/LoginController.java
package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model) {
        // redireciono padrão = área do cliente; o JS troca para admin se o usuário escolher
        model.addAttribute("redirectDefault", "/cliente/conta");
        return "pages/auth/login";
    }
}
