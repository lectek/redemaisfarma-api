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

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PaymentMethodService paymentMethodService;

    public ClientePedidosController(PedidoRepository pedidoRepository,
                                    UsuarioRepository usuarioRepository,
                                    PaymentMethodService paymentMethodService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public String listar(Model model, Authentication auth) {
        ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            model.addAttribute("pedidos", List.of());
            return "pages/cliente/pedidos/lista";
        }
        List<PedidoResumoView> pedidos = pedidoRepository
                .listarPorCliente(identidade.email(), identidade.cpf())
                .stream()
                .map(p -> PedidoResumoView.from(p, paymentMethodService))
                .toList();
        model.addAttribute("pedidos", pedidos);
        return "pages/cliente/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model, Authentication auth, RedirectAttributes ra) {
        ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            ra.addFlashAttribute("errorMessage", "Usuario nao encontrado.");
            return "redirect:/cliente/pedidos";
        }
        Optional<PedidoEntity> pedido = pedidoRepository.buscarDetalhePorCliente(id, identidade.email(), identidade.cpf());
        if (pedido.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Pedido nao encontrado.");
            return "redirect:/cliente/pedidos";
        }
        model.addAttribute("pedido", PedidoDetalheView.from(pedido.get(), paymentMethodService));
        return "pages/cliente/pedidos/detalhe";
    }

    private Optional<UsuarioEntity> localizarUsuario(Authentication auth) {
        if (auth == null || auth.getName() == null) return Optional.empty();
        return usuarioRepository.findByEmailOrCpf(auth.getName());
    }

    private ClienteIdentidade resolveIdentidade(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        Optional<UsuarioEntity> usuario = localizarUsuario(auth);
        if (usuario.isPresent()) {
            UsuarioEntity u = usuario.get();
            return new ClienteIdentidade(u.getEmail(), u.getCpf());
        }
        String principal = auth.getName().trim();
        return new ClienteIdentidade(principal, principal);
    }

    private record ClienteIdentidade(String email, String cpf) {}

    public record PedidoResumoView(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento
    ) {
        static PedidoResumoView from(PedidoEntity p, PaymentMethodService paymentMethodService) {
            String metodo = resolveMetodoLabel(p, paymentMethodService);
            return new PedidoResumoView(
                    p.getId(),
                    p.getData(),
                    p.getTotal(),
                    p.getStatus() != null ? p.getStatus().name() : "DESCONHECIDO",
                    metodo
            );
        }
    }

    public record ItemView(
            String produto,
            Integer quantidade,
            BigDecimal subtotal
    ) {
        static ItemView from(ItemPedidoEntity i) {
            String nome = i.getProduto() != null ? i.getProduto().getNome() : "Produto";
            return new ItemView(nome, i.getQuantidade(), i.getSubtotal());
        }
    }

    public record PedidoDetalheView(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento,
            List<ItemView> itens
    ) {
        static PedidoDetalheView from(PedidoEntity p, PaymentMethodService paymentMethodService) {
            String metodo = resolveMetodoLabel(p, paymentMethodService);
            List<ItemView> itens = p.getItens() == null
                    ? List.of()
                    : p.getItens().stream().map(ItemView::from).toList();
            return new PedidoDetalheView(
                    p.getId(),
                    p.getData(),
                    p.getTotal(),
                    p.getStatus() != null ? p.getStatus().name() : "DESCONHECIDO",
                    metodo,
                    itens
            );
        }
    }

    private static String resolveMetodoLabel(PedidoEntity p, PaymentMethodService paymentMethodService) {
        if (p.getMetodoPagamento() != null && !p.getMetodoPagamento().isBlank()) {
            return paymentMethodService.resolveLabel(p.getMetodoPagamento());
        }
        return p.getTipoPagamento() != null ? p.getTipoPagamento().name() : "DESCONHECIDO";
    }
}
