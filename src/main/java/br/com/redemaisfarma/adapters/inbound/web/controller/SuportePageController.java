package br.com.redemaisfarma.adapters.inbound.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class SuportePageController {

    /**
     * Renders the customer support page.
     *
     * @return support view
     */
    @GetMapping(value = {"/suporte"})
    public String paginaSuporte() {
        return "suporte";
    }
}
