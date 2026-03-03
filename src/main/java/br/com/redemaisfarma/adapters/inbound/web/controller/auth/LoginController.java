package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class LoginController {

    /**
     * Feature flag that controls OAuth2 login options in the page.
     */
    @Value("${app.security.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    /**
     * Renders login page.
     *
     * @param model web model
     * @return login view
     */
    @GetMapping("/auth/login")
    public String login(final Model model) {
        // Default redirect is customer area.
        // JS swaps to admin by selected profile.
        model.addAttribute("redirectDefault", "/cliente/conta");
        model.addAttribute("oauth2Enabled", oauth2Enabled);
        return "pages/auth/login";
    }
}
