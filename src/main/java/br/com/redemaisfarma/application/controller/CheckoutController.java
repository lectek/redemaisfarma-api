package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.service.CartService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.support.DeliveryCodeGenerator;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
@Validated
public class CheckoutController {

    private static final String SESSION_PAYMENT_VALUE = "checkoutPaymentValue";
    private static final String SESSION_PAYMENT_LABEL = "checkoutPaymentLabel";
    private static final String SESSION_PEDIDO_ID = "checkoutPedidoId";
    private static final String SESSION_DELIVERY_CODE = "checkoutDeliveryCode";
    private static final String SESSION_DELIVERY_ADDRESS = "checkoutDeliveryAddress";

    private final PaymentMethodService paymentMethodService;
    private final CartService cartService;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoJPARepository pedidoRepository;

    public CheckoutController(PaymentMethodService paymentMethodService,
                              CartService cartService,
                              ClienteRepository clienteRepository,
                              UsuarioRepository usuarioRepository,
                              PedidoJPARepository pedidoRepository) {
        this.paymentMethodService = paymentMethodService;
        this.cartService = cartService;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @PostMapping("/pedido/finalizar")
    public String finalizarPedido(
            @RequestParam("nome") @NotBlank String nome,
            @RequestParam("cpf") @NotBlank String cpf,
            @RequestParam("email") @NotBlank String email,
            @RequestParam("pagamento") @NotBlank String pagamento,
            Authentication auth,
            HttpSession session,
            RedirectAttributes ra) {

        if (!paymentMethodService.isActiveValue(pagamento)) {
            ra.addFlashAttribute("error", "Metodo de pagamento invalido ou indisponivel.");
            return "redirect:/checkout";
        }

        String label = paymentMethodService.resolveLabel(pagamento);
        TipoPagamento tipoPagamento = resolveTipoPagamento(pagamento);
        if (tipoPagamento == null) {
            ra.addFlashAttribute("error", "Metodo de pagamento invalido.");
            return "redirect:/checkout";
        }

        Optional<ClienteEntity> clienteOpt = resolveClienteLogado(auth, normalize(email), normalize(cpf));
        if (clienteOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Cliente nao encontrado. Faca login novamente.");
            return "redirect:/checkout";
        }

        CartService.CartOrderData orderData = cartService.buildOrderData(session);
        if (orderData.getInvalidItems() != null && !orderData.getInvalidItems().isEmpty()) {
            ra.addFlashAttribute("error", "Existem itens indisponiveis no carrinho. Ajuste antes de continuar.");
            return "redirect:/carrinho";
        }
        if (orderData.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "Seu carrinho esta vazio.");
            return "redirect:/carrinho";
        }

        ClienteEntity cliente = clienteOpt.get();
        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTotal(orderData.getTotal());
        pedido.setTipoPagamento(tipoPagamento);
        pedido.setMetodoPagamento(pagamento);
        pedido.setEnderecoEntrega(resolveEnderecoEntrega(auth));
        pedido.setCodigoEntrega(DeliveryCodeGenerator.nextCode());
        pedido.setCodigoEntregaGeradoEm(LocalDateTime.now());
        pedido.setCodigoEntregaConfirmadoEm(null);
        orderData.getItems().forEach(pedido::addItem);
        PedidoEntity saved = pedidoRepository.save(pedido);

        session.setAttribute(SESSION_PAYMENT_VALUE, pagamento);
        session.setAttribute(SESSION_PAYMENT_LABEL, label);
        session.setAttribute(SESSION_PEDIDO_ID, saved.getId());
        session.setAttribute(SESSION_DELIVERY_CODE, saved.getCodigoEntrega());
        session.setAttribute(SESSION_DELIVERY_ADDRESS, saved.getEnderecoEntrega());
        cartService.clear(session);
        ra.addFlashAttribute("success", "Pedido registrado com o metodo de pagamento.");
        return "redirect:/checkout/confirmacao";
    }

    @GetMapping("/checkout/confirmacao")
    public String confirmacao(Model model, HttpSession session) {
        Object value = session.getAttribute(SESSION_PAYMENT_VALUE);
        Object label = session.getAttribute(SESSION_PAYMENT_LABEL);
        if (value != null) {
            model.addAttribute("paymentMethodValue", value.toString());
        }
        if (label != null) {
            model.addAttribute("paymentMethodLabel", label.toString());
        }
        Object pedidoId = session.getAttribute(SESSION_PEDIDO_ID);
        if (pedidoId != null) {
            model.addAttribute("pedidoId", pedidoId.toString());
        }
        Object deliveryCode = session.getAttribute(SESSION_DELIVERY_CODE);
        if (deliveryCode != null) {
            model.addAttribute("codigoEntrega", deliveryCode.toString());
        }
        Object deliveryAddress = session.getAttribute(SESSION_DELIVERY_ADDRESS);
        if (deliveryAddress != null) {
            model.addAttribute("enderecoEntrega", deliveryAddress.toString());
        }
        session.removeAttribute(SESSION_PAYMENT_VALUE);
        session.removeAttribute(SESSION_PAYMENT_LABEL);
        session.removeAttribute(SESSION_PEDIDO_ID);
        session.removeAttribute(SESSION_DELIVERY_CODE);
        session.removeAttribute(SESSION_DELIVERY_ADDRESS);
        return "pages/cliente/checkout/confirmacao";
    }

    private Optional<ClienteEntity> resolveClienteLogado(Authentication auth, String email, String cpf) {
        if (auth == null || auth.getName() == null) {
            return Optional.empty();
        }
        String ident = normalize(auth.getName());
        Optional<ClienteEntity> byEmail = clienteRepository.findByEmailIgnoreCase(ident);
        Optional<ClienteEntity> byCpf = clienteRepository.findByCpf(ident);
        Optional<ClienteEntity> cliente = byEmail.isPresent() ? byEmail : byCpf;
        if (cliente.isEmpty()) {
            return Optional.empty();
        }
        ClienteEntity found = cliente.get();
        if (!email.isBlank() && !email.equalsIgnoreCase(found.getEmail())) {
            return Optional.empty();
        }
        if (!cpf.isBlank() && !normalizeCpf(cpf).equals(normalizeCpf(found.getCpf()))) {
            return Optional.empty();
        }
        return Optional.of(found);
    }

    private TipoPagamento resolveTipoPagamento(String value) {
        if (value == null) return null;
        return switch (value) {
            case "pix" -> TipoPagamento.PIX;
            case "boleto" -> TipoPagamento.BOLETO;
            case "credito" -> TipoPagamento.CARTAO_CREDITO;
            case "debito" -> TipoPagamento.CARTAO_DEBITO;
            case "dinheiro" -> TipoPagamento.DINHEIRO;
            default -> value.startsWith("custom:") ? TipoPagamento.CUSTOM : null;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeCpf(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private String resolveEnderecoEntrega(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return usuarioRepository.findByEmailOrCpf(auth.getName())
                .map(UsuarioEntity::getEndereco)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(v -> v.length() > 255 ? v.substring(0, 255) : v)
                .orElse(null);
    }
}
