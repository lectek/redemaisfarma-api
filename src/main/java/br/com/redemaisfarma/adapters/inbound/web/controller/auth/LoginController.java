// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/auth/LoginController.java
package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @Value("${app.security.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    @GetMapping("/auth/login")
    public String login(Model model) {
        // default redirect is customer area; JS swaps to admin when profile changes
        model.addAttribute("redirectDefault", "/cliente/conta");
        model.addAttribute("oauth2Enabled", oauth2Enabled);
        return "pages/auth/login";
    }
}
