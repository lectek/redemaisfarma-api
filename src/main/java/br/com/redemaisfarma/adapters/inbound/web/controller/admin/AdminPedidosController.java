package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public final class AdminPedidosController {

    /**
     * First page index used for recent orders listing.
     */
    private static final int RECENT_PAGE_INDEX = 0;

    /**
     * Number of recent orders loaded in listing page.
     */
    private static final int RECENT_PAGE_SIZE = 200;

    /**
     * Number of digits used in order number formatting.
     */
    private static final int ORDER_NUMBER_DIGITS = 4;

    /**
     * Decimal scale used in derived unit price.
     */
    private static final int PRECO_SCALE = 2;

    /**
     * Default text for unknown customer or product names.
     */
    private static final String DEFAULT_LABEL = "Cliente";

    /**
     * Default text for unknown values.
     */
    private static final String DEFAULT_UNKNOWN = "-";

    /**
     * Default text for unknown status.
     */
    private static final String STATUS_DESCONHECIDO = "DESCONHECIDO";

    /**
     * Default text for missing delivery address.
     */
    private static final String ENDERECO_NAO_INFORMADO = "Nao informado";

    /**
     * Repository for order reads.
     */
    private final PedidoRepository pedidoRepository;

    /**
     * Service used to resolve payment method labels.
     */
    private final PaymentMethodService paymentMethodService;

    /**
     * Creates controller with dependencies.
     *
     * @param pedidoRepositoryValue order repository
     * @param paymentMethodServiceValue payment method service
     */
    public AdminPedidosController(
            final PedidoRepository pedidoRepositoryValue,
            final PaymentMethodService paymentMethodServiceValue
    ) {
        this.pedidoRepository = pedidoRepositoryValue;
        this.paymentMethodService = paymentMethodServiceValue;
    }

    /**
     * Lists recent orders with optional text and status filters.
     *
     * @param q optional text query
     * @param status optional status filter
     * @param model view model
     * @return orders list view
     */
    @GetMapping
    public String listar(
            @RequestParam(name = "q", required = false) final String q,
            @RequestParam(name = "status", required = false)
            final String status,
            final Model model
    ) {
        final List<PedidoEntity> pedidos = pedidoRepository.listarRecentes(
                PageRequest.of(RECENT_PAGE_INDEX, RECENT_PAGE_SIZE)
        );
        final List<PedidoListaView> lista = pedidos.stream()
                .filter(pedido -> matchesStatus(pedido, status))
                .filter(pedido -> matchesQuery(pedido, q))
                .map(PedidoListaView::from)
                .toList();
        model.addAttribute("pedidos", lista);
        return "pages/admin/pedidos/lista";
    }

    /**
     * Shows details for one order.
     *
     * @param id order id
     * @param model view model
     * @param ra redirect attributes
     * @return order detail view or redirect
     */
    @GetMapping("/{id}")
    public String detalhe(
            @PathVariable("id") final Long id,
            final Model model,
            final RedirectAttributes ra
    ) {
        final Optional<PedidoEntity> pedido = pedidoRepository
                .buscarDetalheAdmin(id);
        if (pedido.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Pedido nao encontrado.");
            return "redirect:/admin/pedidos";
        }
        model.addAttribute(
                "pedido",
                PedidoAdminDetalheView.from(pedido.get(), paymentMethodService)
        );
        return "pages/admin/pedidos/detalhe";
    }

    /**
     * Checks if order matches selected status.
     *
     * @param pedido order entity
     * @param raw raw status filter
     * @return true when order matches the filter
     */
    private static boolean matchesStatus(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final StatusPedido status = pedido.getStatus();
        if (status == null) {
            return false;
        }
        final String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("AGUARDANDO".equals(normalized)) {
            return status == StatusPedido.AGUARDANDO_PAGAMENTO;
        }
        return normalized.equals(status.name());
    }

    /**
     * Checks if order matches text query.
     *
     * @param pedido order entity
     * @param raw raw query
     * @return true when order matches query
     */
    private static boolean matchesQuery(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final String query = raw.trim().toLowerCase(Locale.ROOT);
        if (pedido.getId() != null
                && pedido.getId().toString().contains(query)) {
            return true;
        }
        if (pedido.getCliente() != null) {
            final String nome = pedido.getCliente().getNome();
            final String email = pedido.getCliente().getEmail();
            if (nome != null && nome.toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
            if (email != null
                    && email.toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns user-friendly status label.
     *
     * @param status order status
     * @return status label
     */
    private static String statusLabel(final StatusPedido status) {
        if (status == null) {
            return "Desconhecido";
        }
        return switch (status) {
            case ABERTO -> "Aberto";
            case AGUARDANDO_PAGAMENTO -> "Aguardando pagamento";
            case PAGO -> "Pago";
            case ENVIADO -> "Enviado";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }

    /**
     * Resolves item unit price from product price or subtotal fallback.
     *
     * @param item order item
     * @return unit price
     */
    private static BigDecimal resolvePreco(final ItemPedidoEntity item) {
        final ProdutoEntity produto = item.getProduto();
        if (produto != null && produto.getPrecoVenda() != null) {
            return produto.getPrecoVenda();
        }

        final Integer qtd = item.getQuantidade();
        final BigDecimal subtotal = item.getSubtotal();
        if (qtd != null && qtd > 0 && subtotal != null) {
            return subtotal.divide(
                    BigDecimal.valueOf(qtd),
                    PRECO_SCALE,
                    RoundingMode.HALF_UP
            );
        }
        return BigDecimal.ZERO;
    }

    /**
     * Resolves payment method label from saved method or enum fallback.
     *
     * @param pedido order entity
     * @param paymentMethodServiceValue payment method service
     * @param tipoPagamento payment enum
     * @return resolved label
     */
    private static String resolveMetodoLabel(
            final PedidoEntity pedido,
            final PaymentMethodService paymentMethodServiceValue,
            final TipoPagamento tipoPagamento
    ) {
        if (pedido.getMetodoPagamento() != null
                && !pedido.getMetodoPagamento().isBlank()) {
            return paymentMethodServiceValue.resolveLabel(
                    pedido.getMetodoPagamento()
            );
        }
        return tipoPagamento != null ? tipoPagamento.name() : "NAO_INFORMADO";
    }

    /**
     * Payment view model.
     *
     * @param metodo method label
     * @param status status label
     */
    public record PagamentoView(
            String metodo,
            String status
    ) {
    }

    /**
     * Order item view model.
     *
     * @param nome product name
     * @param qtd quantity
     * @param preco unit price
     * @param subtotal item subtotal
     */
    public record ItemView(
            String nome,
            Integer qtd,
            BigDecimal preco,
            BigDecimal subtotal
    ) {

        /**
         * Creates view model from entity.
         *
         * @param item order item
         * @return item view
         */
        static ItemView from(final ItemPedidoEntity item) {
            final String nome = item.getProduto() != null
                    && item.getProduto().getNome() != null
                    ? item.getProduto().getNome()
                    : "Produto";
            return new ItemView(
                    nome,
                    item.getQuantidade(),
                    resolvePreco(item),
                    item.getSubtotal()
            );
        }
    }

    /**
     * Order detail view model.
     *
     * @param id order id
     * @param numero order number
     * @param clienteNome customer name
     * @param clienteEmail customer email
     * @param enderecoEntrega delivery address
     * @param pagamento payment payload
     * @param total total value
     * @param status raw status
     * @param itens item list
     */
    public record PedidoAdminDetalheView(
            Long id,
            String numero,
            String clienteNome,
            String clienteEmail,
            String enderecoEntrega,
            PagamentoView pagamento,
            BigDecimal total,
            String status,
            List<ItemView> itens
    ) {

        /**
         * Creates detail view model from entity.
         *
         * @param pedido order entity
         * @param paymentMethodServiceValue payment method service
         * @return detail view
         */
        static PedidoAdminDetalheView from(
                final PedidoEntity pedido,
                final PaymentMethodService paymentMethodServiceValue
        ) {
            final String clienteNome = pedido.getCliente() != null
                    && pedido.getCliente().getNome() != null
                    ? pedido.getCliente().getNome()
                    : DEFAULT_LABEL;
            final String clienteEmail = pedido.getCliente() != null
                    && pedido.getCliente().getEmail() != null
                    ? pedido.getCliente().getEmail()
                    : DEFAULT_UNKNOWN;
            final String numero = pedido.getId() != null
                    ? String.format(
                            "%0" + ORDER_NUMBER_DIGITS + "d",
                            pedido.getId()
                    )
                    : DEFAULT_UNKNOWN;
            final String statusRaw = pedido.getStatus() != null
                    ? pedido.getStatus().name()
                    : STATUS_DESCONHECIDO;
            final TipoPagamento tipoPagamento = pedido.getTipoPagamento();
            final String metodo = resolveMetodoLabel(
                    pedido,
                    paymentMethodServiceValue,
                    tipoPagamento
            );
            final PagamentoView pagamento =
                    new PagamentoView(metodo, statusLabel(pedido.getStatus()));
            final List<ItemView> itens = pedido.getItens() == null
                    ? List.of()
                    : pedido.getItens().stream().map(ItemView::from).toList();
            return new PedidoAdminDetalheView(
                    pedido.getId(),
                    numero,
                    clienteNome,
                    clienteEmail,
                    ENDERECO_NAO_INFORMADO,
                    pagamento,
                    pedido.getTotal(),
                    statusRaw,
                    itens
            );
        }
    }

    /**
     * Order list view model.
     *
     * @param id order id
     * @param numero order number
     * @param clienteNome customer name
     * @param data order date
     * @param total total value
     * @param status raw status
     * @param statusLabel human-readable status
     */
    public record PedidoListaView(
            Long id,
            String numero,
            String clienteNome,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String statusLabel
    ) {

        /**
         * Creates list view from entity.
         *
         * @param pedido order entity
         * @return list view
         */
        static PedidoListaView from(final PedidoEntity pedido) {
            final String cliente = pedido.getCliente() != null
                    && pedido.getCliente().getNome() != null
                    ? pedido.getCliente().getNome()
                    : DEFAULT_LABEL;
            final String numero = pedido.getId() != null
                    ? String.format(
                            "%0" + ORDER_NUMBER_DIGITS + "d",
                            pedido.getId()
                    )
                    : DEFAULT_UNKNOWN;
            final String status = pedido.getStatus() != null
                    ? pedido.getStatus().name()
                    : STATUS_DESCONHECIDO;
            final String statusLabelText = AdminPedidosController.statusLabel(
                    pedido.getStatus()
            );
            return new PedidoListaView(
                    pedido.getId(),
                    numero,
                    cliente,
                    pedido.getData(),
                    pedido.getTotal(),
                    status,
                    statusLabelText
            );
        }
    }
}
