// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/admin/RelatorioClientesPageController.java
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.MediaType;

@Controller
public class RelatorioClientesPageController {

    @GetMapping(value = "/admin/relatorios/clientes", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        // View Thymeleaf da página do relatório
        return "pages/admin/relatorios/clientes";
    }
}
