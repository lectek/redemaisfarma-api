package br.com.redemaisfarma.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador responsável por exibir as páginas públicas da loja. Faz parte da camada de entrada (inbound) da
 * arquitetura hexagonal.
 */
@Controller
public class HomeController {

    /**
     * Página inicial da loja
     */
    @GetMapping("/")
    public String exibirPaginaInicial(Model model) {
        return "pages/index";
    }

    /**
     * Página de catálogo de produtos
     */
    @GetMapping("/catalogo")
    public String exibirCatalogo(Model model) {
        return "pages/catalogo";
    }

    /**
     * Página de checkout
     */
    @GetMapping("/checkout")
    public String exibirCheckout(Model model) {
        return "pages/checkout";
    }

    /**
     * Página do carrinho de compras
     */
    @GetMapping("/carrinho")
    public String exibirCarrinho(Model model) {
        return "pages/carrinho";
    }

    /**
     * Página institucional "Sobre nós"
     */
    @GetMapping("/sobre")
    public String exibirSobre(Model model) {
        return "pages/sobre";
    }
}
