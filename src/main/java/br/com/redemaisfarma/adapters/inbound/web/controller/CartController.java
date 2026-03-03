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

    /**
     * Service responsible for cart operations.
     */
    private final CartService cartService;

    /**
     * Creates controller with cart service dependency.
     *
     * @param service cart service
     */
    public CartController(final CartService service) {
        this.cartService = service;
    }

    /**
     * Adds an item to the session cart.
     *
     * @param produtoId product id
     * @param quantidade requested quantity
     * @param ra redirect attributes
     * @param session current session
     * @return redirect to cart page
     */
    @PostMapping("/carrinho/adicionar")
    public String adicionar(
            @RequestParam("produtoId") @NotNull final Long produtoId,
            @RequestParam(value = "quantidade", defaultValue = "1")
            @Min(1) final int quantidade,
            final RedirectAttributes ra,
            final HttpSession session
    ) {
        final CartValidationService.CartValidationResult validation =
                cartService.validateAdd(session, produtoId, quantidade);
        if (!validation.valid()) {
            ra.addFlashAttribute("error", validation.message());
            return "redirect:/carrinho";
        }
        cartService.addItem(session, produtoId, quantidade);
        ra.addFlashAttribute("success", "Produto adicionado ao carrinho.");
        return "redirect:/carrinho";
    }

    /**
     * Updates an existing item quantity in the cart.
     *
     * @param produtoId product id
     * @param quantidade requested quantity
     * @param ra redirect attributes
     * @param session current session
     * @return redirect to cart page
     */
    @PostMapping("/carrinho/atualizar")
    public String atualizar(
            @RequestParam("produtoId") @NotNull final Long produtoId,
            @RequestParam("quantidade") final int quantidade,
            final RedirectAttributes ra,
            final HttpSession session
    ) {
        cartService.updateItem(session, produtoId, quantidade);
        ra.addFlashAttribute("success", "Carrinho atualizado.");
        return "redirect:/carrinho";
    }

    /**
     * Removes an item from the cart.
     *
     * @param produtoId product id
     * @param ra redirect attributes
     * @param session current session
     * @return redirect to cart page
     */
    @PostMapping("/carrinho/remover")
    public String remover(
            @RequestParam("produtoId") @NotNull final Long produtoId,
            final RedirectAttributes ra,
            final HttpSession session
    ) {
        cartService.removeItem(session, produtoId);
        ra.addFlashAttribute("success", "Item removido.");
        return "redirect:/carrinho";
    }
}
