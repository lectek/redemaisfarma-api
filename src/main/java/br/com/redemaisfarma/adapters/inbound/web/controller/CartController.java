package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.service.CartService;
import br.com.redemaisfarma.application.service.validation.CartValidationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Validated
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/carrinho/adicionar")
    public String adicionar(
            @RequestParam("produtoId") @NotNull Long produtoId,
            @RequestParam(value = "quantidade", defaultValue = "1") @Min(1) int quantidade,
            RedirectAttributes ra,
            HttpSession session) {
        CartValidationService.CartValidationResult validation = cartService.validateAdd(session, produtoId, quantidade);
        if (!validation.valid()) {
            ra.addFlashAttribute("error", validation.message());
            return "redirect:/carrinho";
        }
        cartService.addItem(session, produtoId, quantidade);
        ra.addFlashAttribute("success", "Produto adicionado ao carrinho.");
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/atualizar")
    public String atualizar(
            @RequestParam("produtoId") @NotNull Long produtoId,
            @RequestParam("quantidade") int quantidade,
            RedirectAttributes ra,
            HttpSession session) {
        cartService.updateItem(session, produtoId, quantidade);
        ra.addFlashAttribute("success", "Carrinho atualizado.");
        return "redirect:/carrinho";
    }

    @PostMapping("/carrinho/remover")
    public String remover(
            @RequestParam("produtoId") @NotNull Long produtoId,
            RedirectAttributes ra,
            HttpSession session) {
        cartService.removeItem(session, produtoId);
        ra.addFlashAttribute("success", "Item removido.");
        return "redirect:/carrinho";
    }
}
