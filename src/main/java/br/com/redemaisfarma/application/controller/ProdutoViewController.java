package br.com.redemaisfarma.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável por exibir a página HTML do catálogo de produtos (frontend). Este controller NÃO retorna JSON,
 * apenas renderiza páginas com Thymeleaf.
 */
@Controller
public class ProdutoViewController {

    /**
     * Exibe a página HTML com a lista de produtos. URL: http://localhost:8080/produtos
     */
    @GetMapping("/produtos")
    public String exibirCatalogoHtml() {
        return "pages/produtos"; // Corresponde a templates/pages/produtos.html
    }
}
