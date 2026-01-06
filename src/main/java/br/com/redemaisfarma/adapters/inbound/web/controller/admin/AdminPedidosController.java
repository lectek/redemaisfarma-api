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
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidosController {

    private final PedidoRepository pedidoRepository;
    private final PaymentMethodService paymentMethodService;

    public AdminPedidosController(PedidoRepository pedidoRepository, PaymentMethodService paymentMethodService) {
        this.pedidoRepository = pedidoRepository;
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "q", required = false) String q,
                         @RequestParam(name = "status", required = false) String status,
                         Model model) {
        List<PedidoEntity> pedidos = pedidoRepository.listarRecentes(PageRequest.of(0, 200));
        List<PedidoListaView> lista = pedidos.stream()
                .filter(p -> matchesStatus(p, status))
                .filter(p -> matchesQuery(p, q))
                .map(PedidoListaView::from)
                .toList();
        model.addAttribute("pedidos", lista);
        return "pages/admin/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<PedidoEntity> pedido = pedidoRepository.buscarDetalheAdmin(id);
        if (pedido.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Pedido nao encontrado.");
            return "redirect:/admin/pedidos";
        }
        model.addAttribute("pedido", PedidoAdminDetalheView.from(pedido.get(), paymentMethodService));
        return "pages/admin/pedidos/detalhe";
    }

    private static boolean matchesStatus(PedidoEntity p, String raw) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        StatusPedido status = p.getStatus();
        if (status == null) {
            return false;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("AGUARDANDO".equals(normalized)) {
            return status == StatusPedido.AGUARDANDO_PAGAMENTO;
        }
        return normalized.equals(status.name());
    }

    private static boolean matchesQuery(PedidoEntity p, String raw) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        String q = raw.trim().toLowerCase(Locale.ROOT);
        if (p.getId() != null && p.getId().toString().contains(q)) {
            return true;
        }
        if (p.getCliente() != null) {
            String nome = p.getCliente().getNome();
            String email = p.getCliente().getEmail();
            if (nome != null && nome.toLowerCase(Locale.ROOT).contains(q)) {
                return true;
            }
            if (email != null && email.toLowerCase(Locale.ROOT).contains(q)) {
                return true;
            }
        }
        return false;
    }

    private static String statusLabel(StatusPedido status) {
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

    private static BigDecimal resolvePreco(ItemPedidoEntity item) {
        ProdutoEntity produto = item.getProduto();
        if (produto != null && produto.getPrecoVenda() != null) {
            return produto.getPrecoVenda();
        }
        Integer qtd = item.getQuantidade();
        BigDecimal subtotal = item.getSubtotal();
        if (qtd != null && qtd > 0 && subtotal != null) {
            return subtotal.divide(BigDecimal.valueOf(qtd), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public record PagamentoView(
            String metodo,
            String status
    ) {}

    public record ItemView(
            String nome,
            Integer qtd,
            BigDecimal preco,
            BigDecimal subtotal
    ) {
        static ItemView from(ItemPedidoEntity item) {
            String nome = item.getProduto() != null && item.getProduto().getNome() != null
                    ? item.getProduto().getNome()
                    : "Produto";
            return new ItemView(nome, item.getQuantidade(), resolvePreco(item), item.getSubtotal());
        }
    }

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
        static PedidoAdminDetalheView from(PedidoEntity p, PaymentMethodService paymentMethodService) {
            String clienteNome = p.getCliente() != null && p.getCliente().getNome() != null
                    ? p.getCliente().getNome()
                    : "Cliente";
            String clienteEmail = p.getCliente() != null && p.getCliente().getEmail() != null
                    ? p.getCliente().getEmail()
                    : "-";
            String numero = p.getId() != null ? String.format("%04d", p.getId()) : "-";
            String statusRaw = p.getStatus() != null ? p.getStatus().name() : "DESCONHECIDO";
            TipoPagamento tipoPagamento = p.getTipoPagamento();
            String metodo = resolveMetodoLabel(p, paymentMethodService, tipoPagamento);
            PagamentoView pagamento = new PagamentoView(metodo, statusLabel(p.getStatus()));
            List<ItemView> itens = p.getItens() == null
                    ? List.of()
                    : p.getItens().stream().map(ItemView::from).toList();
            return new PedidoAdminDetalheView(
                    p.getId(),
                    numero,
                    clienteNome,
                    clienteEmail,
                    "Nao informado",
                    pagamento,
                    p.getTotal(),
                    statusRaw,
                    itens
            );
        }
    }

    private static String resolveMetodoLabel(PedidoEntity p,
                                             PaymentMethodService paymentMethodService,
                                             TipoPagamento tipoPagamento) {
        if (p.getMetodoPagamento() != null && !p.getMetodoPagamento().isBlank()) {
            return paymentMethodService.resolveLabel(p.getMetodoPagamento());
        }
        return tipoPagamento != null ? tipoPagamento.name() : "NAO_INFORMADO";
    }

    public record PedidoListaView(
            Long id,
            String numero,
            String clienteNome,
            java.time.LocalDateTime data,
            BigDecimal total,
            String status,
            String statusLabel
    ) {
        static PedidoListaView from(PedidoEntity p) {
            String cliente = p.getCliente() != null && p.getCliente().getNome() != null
                    ? p.getCliente().getNome()
                    : "Cliente";
            String numero = p.getId() != null ? String.format("%04d", p.getId()) : "-";
            String status = p.getStatus() != null ? p.getStatus().name() : "DESCONHECIDO";
            String statusLabel = AdminPedidosController.statusLabel(p.getStatus());
            return new PedidoListaView(p.getId(), numero, cliente, p.getData(), p.getTotal(), status, statusLabel);
        }
    }
}
