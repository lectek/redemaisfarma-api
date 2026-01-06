package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteFavoritoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteFavoritoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.service.CartService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.view.CartItemVM;
import br.com.redemaisfarma.application.view.CartSummaryVM;
import br.com.redemaisfarma.application.view.PaymentMethodVM;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cliente/me")
@Validated
public class ClienteSelfApiController {
    private static final long AVATAR_MAX_BYTES = 2L * 1024L * 1024L;

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final PaymentMethodService paymentMethodService;
    private final ImageStorageService imageStorageService;
    private final CartService cartService;
    private final ClienteRepository clienteRepository;
    private final PedidoJPARepository pedidoJPARepository;
    private final ClienteFavoritoRepository favoritoRepository;
    private final ClienteNotificacaoRepository notificacaoRepository;
    private final ProdutoRepository produtoRepository;

    public ClienteSelfApiController(UsuarioRepository usuarioRepository,
                                    PedidoRepository pedidoRepository,
                                    PaymentMethodService paymentMethodService,
                                    ImageStorageService imageStorageService,
                                    CartService cartService,
                                    ClienteRepository clienteRepository,
                                    PedidoJPARepository pedidoJPARepository,
                                    ClienteFavoritoRepository favoritoRepository,
                                    ClienteNotificacaoRepository notificacaoRepository,
                                    ProdutoRepository produtoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.paymentMethodService = paymentMethodService;
        this.imageStorageService = imageStorageService;
        this.cartService = cartService;
        this.clienteRepository = clienteRepository;
        this.pedidoJPARepository = pedidoJPARepository;
        this.favoritoRepository = favoritoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    public ClienteMeResponse me(Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return ClienteMeResponse.from(usuario);
    }

    @PutMapping
    public ClienteMeResponse atualizar(@Valid @RequestBody ClienteMeUpdateRequest req, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String emailNormalizado = normalizarEmail(req.email());
        if (emailNormalizado == null || emailNormalizado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail valido.");
        }
        usuarioRepository.findByEmailIgnoreCase(emailNormalizado).ifPresent(existente -> {
            if (!existente.getId().equals(usuario.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado.");
            }
        });
        usuario.setNome(req.nome());
        usuario.setEmail(emailNormalizado);
        usuario.setCpf(req.cpf());
        usuario.setTelefone(normalizarTelefone(req.telefone()));
        usuario.setEndereco(normalizarEndereco(req.endereco()));
        usuarioRepository.save(usuario);
        return ClienteMeResponse.from(usuario);
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> atualizarAvatar(@RequestParam("file") MultipartFile file, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem para enviar.");
        }
        if (file.getSize() > AVATAR_MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem acima de 2MB.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato invalido. Use PNG, JPG ou WEBP.");
        }
        try {
            String url = imageStorageService.saveUserAvatar(usuario.getId(), file);
            usuario.setAvatarUrl(url);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(new AvatarResponse(url));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @GetMapping("/pedidos")
    public List<PedidoResumoResponse> listarPedidos(Authentication auth) {
        ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return pedidoRepository.listarPorCliente(identidade.email(), identidade.cpf())
                .stream()
                .map(p -> PedidoResumoResponse.from(p, paymentMethodService))
                .toList();
    }

    @GetMapping("/pedidos/{id}")
    public PedidoDetalheResponse detalhe(@PathVariable Long id, Authentication auth) {
        ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        PedidoEntity pedido = pedidoRepository.buscarDetalhePorCliente(id, identidade.email(), identidade.cpf())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return PedidoDetalheResponse.from(pedido, paymentMethodService);
    }

    @GetMapping("/carrinho")
    public CartSummaryResponse carrinho(HttpSession session) {
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @PostMapping("/carrinho")
    public CartSummaryResponse adicionar(@Valid @RequestBody CartItemRequest req, HttpSession session) {
        CartService.CartValidationResult validation = cartService.validateAdd(req.produtoId(), req.quantidade());
        if (!validation.valid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.message());
        }
        cartService.addItem(session, req.produtoId(), req.quantidade());
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @PutMapping("/carrinho/{produtoId}")
    public CartSummaryResponse atualizar(@PathVariable Long produtoId,
                                         @Valid @RequestBody CartUpdateRequest req,
                                         HttpSession session) {
        cartService.updateItem(session, produtoId, req.quantidade());
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @DeleteMapping("/carrinho/{produtoId}")
    public CartSummaryResponse remover(@PathVariable Long produtoId, HttpSession session) {
        cartService.removeItem(session, produtoId);
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @GetMapping("/checkout/resumo")
    public CheckoutResumoResponse resumo(HttpSession session) {
        CartSummaryVM summary = cartService.buildSummary(session);
        List<PaymentMethodResponse> methods = paymentMethodService.listActiveMethods()
                .stream()
                .map(PaymentMethodResponse::from)
                .toList();
        return new CheckoutResumoResponse(CartSummaryResponse.from(summary), methods);
    }

    @PostMapping("/checkout/finalizar")
    public CheckoutFinalizarResponse finalizar(@Valid @RequestBody CheckoutFinalizarRequest req,
                                               Authentication auth,
                                               HttpSession session) {
        if (!paymentMethodService.isActiveValue(req.pagamento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo de pagamento invalido.");
        }
        TipoPagamento tipoPagamento = resolveTipoPagamento(req.pagamento());
        if (tipoPagamento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo de pagamento invalido.");
        }

        Optional<ClienteEntity> clienteOpt = resolveClienteLogado(auth, normalize(req.email()), normalize(req.cpf()));
        if (clienteOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente nao encontrado.");
        }

        CartService.CartOrderData orderData = cartService.buildOrderData(session);
        if (orderData.getInvalidItems() != null && !orderData.getInvalidItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Existem itens indisponiveis no carrinho.");
        }
        if (orderData.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio.");
        }

        ClienteEntity cliente = clienteOpt.get();
        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTotal(orderData.getTotal());
        pedido.setTipoPagamento(tipoPagamento);
        pedido.setMetodoPagamento(req.pagamento());
        orderData.getItems().forEach(pedido::addItem);
        PedidoEntity saved = pedidoJPARepository.save(pedido);
        cartService.clear(session);

        String label = paymentMethodService.resolveLabel(req.pagamento());
        localizarUsuario(auth).ifPresent(usuario -> {
            String mensagem = "Pedido #" + saved.getId() + " registrado. Pagamento: " + label + ".";
            salvarNotificacao(usuario, "PEDIDO", "Pedido registrado", mensagem);
        });
        return new CheckoutFinalizarResponse(saved.getId(), label);
    }

    @GetMapping("/favoritos")
    public List<FavoritoResponse> favoritos(Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return favoritoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(FavoritoResponse::from)
                .toList();
    }

    @PostMapping("/favoritos")
    public FavoritoResponse adicionarFavorito(@Valid @RequestBody FavoritoRequest req, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (favoritoRepository.existsByUsuarioIdAndProdutoId(usuario.getId(), req.produtoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto ja favoritado.");
        }
        ProdutoEntity produto = produtoRepository.findById(req.produtoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado."));
        ClienteFavoritoEntity favorito = new ClienteFavoritoEntity();
        favorito.setUsuario(usuario);
        favorito.setProduto(produto);
        ClienteFavoritoEntity saved = favoritoRepository.save(favorito);
        return FavoritoResponse.from(saved);
    }

    @DeleteMapping("/favoritos/{produtoId}")
    public ResponseEntity<?> removerFavorito(@PathVariable Long produtoId, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        favoritoRepository.deleteByUsuarioIdAndProdutoId(usuario.getId(), produtoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notificacoes")
    public NotificacoesResponse notificacoes(Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        List<ClienteNotificacaoEntity> notificacoes = notificacaoRepository.findByUsuarioId(usuario.getId());
        long naoLidas = notificacaoRepository.countByUsuarioIdAndLidaFalse(usuario.getId());
        return new NotificacoesResponse(
                notificacoes.stream().map(NotificacaoResponse::from).toList(),
                naoLidas
        );
    }

    @PostMapping("/notificacoes/lidas")
    public ResponseEntity<?> marcarLidas(@Valid @RequestBody NotificacaoLidasRequest req, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (req.ids() == null || req.ids().isEmpty()) {
            return ResponseEntity.ok().build();
        }
        notificacaoRepository.markAsRead(usuario.getId(), req.ids());
        return ResponseEntity.ok().build();
    }

    private Optional<UsuarioEntity> localizarUsuario(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return Optional.empty();
        }
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

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
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

    private String normalizarTelefone(String telefone) {
        if (telefone == null) return null;
        String trimmed = telefone.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizarEndereco(String endereco) {
        if (endereco == null) return null;
        String trimmed = endereco.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private record ClienteIdentidade(String email, String cpf) {}

    public record ClienteMeResponse(
            Long id,
            String nome,
            String email,
            String cpf,
            String telefone,
            String endereco,
            String avatarUrl
    ) {
        static ClienteMeResponse from(UsuarioEntity usuario) {
            return new ClienteMeResponse(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getCpf(),
                    usuario.getTelefone(),
                    usuario.getEndereco(),
                    usuario.getAvatarUrl()
            );
        }
    }

    public record ClienteMeUpdateRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 150) String email,
            String cpf,
            @Size(max = 25) String telefone,
            @Size(max = 200) String endereco
    ) {}

    public record AvatarResponse(String avatarUrl) {}

    public record CartItemRequest(@NotNull Long produtoId, @Positive int quantidade) {}

    public record CartUpdateRequest(@Positive int quantidade) {}

    public record CartItemResponse(
            Long produtoId,
            String nome,
            String imagem,
            BigDecimal preco,
            Integer quantidade,
            BigDecimal subtotal,
            boolean invalido,
            String motivo,
            Integer estoque
    ) {
        static CartItemResponse from(CartItemVM item) {
            return new CartItemResponse(
                    item.produtoId(),
                    item.nome(),
                    item.imagem(),
                    item.precoUnitario(),
                    item.quantidade(),
                    item.subtotal(),
                    item.invalid(),
                    item.issue(),
                    item.estoque()
            );
        }
    }

    public record CartSummaryResponse(
            List<CartItemResponse> items,
            BigDecimal subtotal,
            BigDecimal total,
            boolean hasInvalidItems
    ) {
        static CartSummaryResponse from(CartSummaryVM summary) {
            List<CartItemResponse> items = summary.items().stream().map(CartItemResponse::from).toList();
            return new CartSummaryResponse(items, summary.subtotal(), summary.total(), summary.hasInvalidItems());
        }
    }

    public record PaymentMethodResponse(String value, String label, String type) {
        static PaymentMethodResponse from(PaymentMethodVM vm) {
            return new PaymentMethodResponse(vm.value(), vm.label(), vm.tipo());
        }
    }

    public record CheckoutResumoResponse(
            CartSummaryResponse carrinho,
            List<PaymentMethodResponse> metodosPagamento
    ) {}

    public record CheckoutFinalizarRequest(
            @NotBlank String nome,
            @NotBlank String cpf,
            @NotBlank @Email String email,
            @NotBlank String pagamento
    ) {}

    public record CheckoutFinalizarResponse(Long pedidoId, String paymentMethodLabel) {}

    public record FavoritoRequest(@NotNull Long produtoId) {}

    public record FavoritoResponse(
            Long produtoId,
            String nome,
            String imagem,
            BigDecimal preco,
            LocalDateTime createdAt
    ) {
        static FavoritoResponse from(ClienteFavoritoEntity favorito) {
            ProdutoEntity produto = favorito.getProduto();
            return new FavoritoResponse(
                    produto != null ? produto.getId() : null,
                    produto != null ? produto.getNome() : null,
                    produto != null ? produto.getImagem() : null,
                    produto != null ? produto.getPrecoVenda() : null,
                    favorito.getCreatedAt()
            );
        }
    }

    public record NotificacaoResponse(
            Long id,
            String tipo,
            String titulo,
            String mensagem,
            Boolean lida,
            LocalDateTime createdAt
    ) {
        static NotificacaoResponse from(ClienteNotificacaoEntity notificacao) {
            return new NotificacaoResponse(
                    notificacao.getId(),
                    notificacao.getTipo(),
                    notificacao.getTitulo(),
                    notificacao.getMensagem(),
                    notificacao.getLida(),
                    notificacao.getCreatedAt()
            );
        }
    }

    public record NotificacoesResponse(
            List<NotificacaoResponse> items,
            long unreadCount
    ) {}

    public record NotificacaoLidasRequest(@NotNull Collection<Long> ids) {}

    private void salvarNotificacao(UsuarioEntity usuario, String tipo, String titulo, String mensagem) {
        ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
        notificacao.setUsuario(usuario);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacaoRepository.save(notificacao);
    }

    public record PedidoResumoResponse(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento
    ) {
        static PedidoResumoResponse from(PedidoEntity p, PaymentMethodService paymentMethodService) {
            return new PedidoResumoResponse(
                    p.getId(),
                    p.getData(),
                    p.getTotal(),
                    p.getStatus() != null ? p.getStatus().name() : "DESCONHECIDO",
                    resolveMetodoLabel(p, paymentMethodService)
            );
        }
    }

    public record PedidoItemResponse(
            Long produtoId,
            String produto,
            Integer quantidade,
            BigDecimal subtotal
    ) {
        static PedidoItemResponse from(ItemPedidoEntity i) {
            Long produtoId = i.getProduto() != null ? i.getProduto().getId() : null;
            String nome = i.getProduto() != null ? i.getProduto().getNome() : "Produto";
            return new PedidoItemResponse(produtoId, nome, i.getQuantidade(), i.getSubtotal());
        }
    }

    public record PedidoDetalheResponse(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento,
            List<PedidoItemResponse> itens
    ) {
        static PedidoDetalheResponse from(PedidoEntity p, PaymentMethodService paymentMethodService) {
            List<PedidoItemResponse> itens = p.getItens() == null
                    ? List.of()
                    : p.getItens().stream().map(PedidoItemResponse::from).toList();
            return new PedidoDetalheResponse(
                    p.getId(),
                    p.getData(),
                    p.getTotal(),
                    p.getStatus() != null ? p.getStatus().name() : "DESCONHECIDO",
                    resolveMetodoLabel(p, paymentMethodService),
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
