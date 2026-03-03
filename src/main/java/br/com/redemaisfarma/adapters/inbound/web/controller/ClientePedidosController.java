package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente/pedidos")
public class ClientePedidosController {

    /**
     * Repository used to fetch customer orders.
     */
    private final PedidoRepository pedidoRepository;

    /**
     * Repository used to resolve authenticated user data.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Service used to map payment method ids to labels.
     */
    private final PaymentMethodService paymentMethodService;

    /**
     * Builds the controller with required dependencies.
     *
     * @param pedidoRepo order repository
     * @param usuarioRepo user repository
     * @param paymentService payment method service
     */
    public ClientePedidosController(
            final PedidoRepository pedidoRepo,
            final UsuarioRepository usuarioRepo,
            final PaymentMethodService paymentService
    ) {
        this.pedidoRepository = pedidoRepo;
        this.usuarioRepository = usuarioRepo;
        this.paymentMethodService = paymentService;
    }

    /**
     * Lists all orders for the authenticated customer.
     *
     * @param model model used by thymeleaf
     * @param auth authenticated principal
     * @return customer order list page
     */
    @GetMapping
    public String listar(final Model model, final Authentication auth) {
        final ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            model.addAttribute("pedidos", List.of());
            return "pages/cliente/pedidos/lista";
        }
        final List<PedidoResumoView> pedidos = pedidoRepository
                .listarPorCliente(identidade.email(), identidade.cpf())
                .stream()
                .map(pedido ->
                        PedidoResumoView.from(pedido, paymentMethodService)
                )
                .toList();
        model.addAttribute("pedidos", pedidos);
        return "pages/cliente/pedidos/lista";
    }

    /**
     * Shows one order detail page for the authenticated customer.
     *
     * @param id order identifier
     * @param model model used by thymeleaf
     * @param auth authenticated principal
     * @param redirectAttributes redirect flash attributes
     * @return detail page or redirect to order list
     */
    @GetMapping("/{id}")
    public String detalhe(
            @PathVariable("id") final Long id,
            final Model model,
            final Authentication auth,
            final RedirectAttributes redirectAttributes
    ) {
        final ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Usuario nao encontrado."
            );
            return "redirect:/cliente/pedidos";
        }
        final Optional<PedidoEntity> pedido =
                pedidoRepository.buscarDetalhePorCliente(
                        id,
                        identidade.email(),
                        identidade.cpf()
                );
        if (pedido.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Pedido nao encontrado."
            );
            return "redirect:/cliente/pedidos";
        }
        model.addAttribute(
                "pedido",
                PedidoDetalheView.from(pedido.get(), paymentMethodService)
        );
        return "pages/cliente/pedidos/detalhe";
    }

    /**
     * Resolves the authenticated user from e-mail or CPF principal.
     *
     * @param auth authenticated principal
     * @return user when found
     */
    private Optional<UsuarioEntity> localizarUsuario(
            final Authentication auth
    ) {
        if (auth == null || auth.getName() == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailOrCpf(auth.getName());
    }

    /**
     * Resolves e-mail/cpf identity used to filter customer orders.
     *
     * @param auth authenticated principal
     * @return identity object or null when authentication is invalid
     */
    private ClienteIdentidade resolveIdentidade(final Authentication auth) {
        if (auth == null
                || auth.getName() == null
                || auth.getName().isBlank()) {
            return null;
        }
        final Optional<UsuarioEntity> usuario = localizarUsuario(auth);
        if (usuario.isPresent()) {
            final UsuarioEntity usuarioAtual = usuario.get();
            return new ClienteIdentidade(
                    usuarioAtual.getEmail(),
                    usuarioAtual.getCpf()
            );
        }
        final String principal = auth.getName().trim();
        return new ClienteIdentidade(principal, principal);
    }

    /**
     * Lightweight identity tuple used in customer order queries.
     *
     * @param email user e-mail
     * @param cpf user cpf
     */
    private record ClienteIdentidade(String email, String cpf) { }

    /**
     * List view payload for customer order page.
     *
     * @param id order id
     * @param data order date
     * @param total order total
     * @param status order status text
     * @param metodoPagamento payment method label
     */
    public record PedidoResumoView(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento
    ) {
        /**
         * Builds list view payload from entity.
         *
         * @param pedido order entity
         * @param paymentService payment method service
         * @return summarized order data
         */
        static PedidoResumoView from(
                final PedidoEntity pedido,
                final PaymentMethodService paymentService
        ) {
            final String metodo = resolveMetodoLabel(pedido, paymentService);
            final String statusAtual = pedido.getStatus() != null
                    ? pedido.getStatus().name()
                    : "DESCONHECIDO";
            return new PedidoResumoView(
                    pedido.getId(),
                    pedido.getData(),
                    pedido.getTotal(),
                    statusAtual,
                    metodo
            );
        }
    }

    /**
     * Item view payload for order detail page.
     *
     * @param produto product name
     * @param quantidade item quantity
     * @param subtotal item subtotal
     */
    public record ItemView(
            String produto,
            Integer quantidade,
            BigDecimal subtotal
    ) {
        /**
         * Builds item view payload from order item entity.
         *
         * @param item order item entity
         * @return item view data
         */
        static ItemView from(final ItemPedidoEntity item) {
            final String nome = item.getProduto() != null
                    ? item.getProduto().getNome()
                    : "Produto";
            return new ItemView(
                    nome,
                    item.getQuantidade(),
                    item.getSubtotal()
            );
        }
    }

    /**
     * Detail view payload for customer order detail page.
     *
     * @param id order id
     * @param data order date
     * @param total order total
     * @param status order status text
     * @param metodoPagamento payment method label
     * @param itens order items
     */
    public record PedidoDetalheView(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento,
            String enderecoEntrega,
            String codigoEntrega,
            LocalDateTime codigoEntregaConfirmadoEm,
            List<ItemView> itens
    ) {
        /**
         * Builds detail view payload from order entity.
         *
         * @param pedido order entity
         * @param paymentService payment method service
         * @return detail view data
         */
        static PedidoDetalheView from(
                final PedidoEntity pedido,
                final PaymentMethodService paymentService
        ) {
            final String metodo = resolveMetodoLabel(pedido, paymentService);
            final List<ItemView> itens = pedido.getItens() == null
                    ? List.of()
                    : pedido.getItens().stream().map(ItemView::from).toList();
            final String statusAtual = pedido.getStatus() != null
                    ? pedido.getStatus().name()
                    : "DESCONHECIDO";
            return new PedidoDetalheView(
                    pedido.getId(),
                    pedido.getData(),
                    pedido.getTotal(),
                    statusAtual,
                    metodo,
                    pedido.getEnderecoEntrega(),
                    pedido.getCodigoEntrega(),
                    pedido.getCodigoEntregaConfirmadoEm(),
                    itens
            );
        }
    }

    /**
     * Resolves payment method label for order list/detail views.
     *
     * @param pedido order entity
     * @param paymentService payment method service
     * @return resolved label
     */
    private static String resolveMetodoLabel(
            final PedidoEntity pedido,
            final PaymentMethodService paymentService
    ) {
        if (pedido.getMetodoPagamento() != null
                && !pedido.getMetodoPagamento().isBlank()) {
            return paymentService.resolveLabel(pedido.getMetodoPagamento());
        }
        return pedido.getTipoPagamento() != null
                ? pedido.getTipoPagamento().name()
                : "DESCONHECIDO";
    }
}
