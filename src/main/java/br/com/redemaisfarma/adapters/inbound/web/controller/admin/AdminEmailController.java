package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/admin/email"})
public final class AdminEmailController {

    /**
     * Renders the admin email center page.
     *
     * @return admin email center view
     */
    @GetMapping
    public String index() {
        return "pages/admin/email/central";
    }
}
