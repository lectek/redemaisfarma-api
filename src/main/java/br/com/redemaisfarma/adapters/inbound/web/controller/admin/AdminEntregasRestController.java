package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.config.AppProps;
import br.com.redemaisfarma.application.service.delivery.DeliveryRouteService;
import br.com.redemaisfarma.application.support.DeliveryCodeGenerator;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/entregas")
@Validated
public class AdminEntregasRestController {

    private static final int MIN_PEDIDOS_ROTA = 2;
    private static final int MAX_PEDIDOS_ROTA = 8;

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DeliveryRouteService deliveryRouteService;
    private final AppProps appProps;

    public AdminEntregasRestController(
            final PedidoRepository pedidoRepositoryValue,
            final UsuarioRepository usuarioRepositoryValue,
            final DeliveryRouteService deliveryRouteServiceValue,
            final AppProps appPropsValue
    ) {
        this.pedidoRepository = pedidoRepositoryValue;
        this.usuarioRepository = usuarioRepositoryValue;
        this.deliveryRouteService = deliveryRouteServiceValue;
        this.appProps = appPropsValue;
    }

    @PostMapping("/roteirizar")
    @Transactional
    public RoteirizacaoResponse roteirizar(
            @Valid @RequestBody final RoteirizarRequest request
    ) {
        final List<Long> pedidoIds = uniqueIds(request.pedidoIds());
        if (pedidoIds.size() < MIN_PEDIDOS_ROTA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe ao menos 2 pedidos para roteirizar."
            );
        }
        if (pedidoIds.size() > MAX_PEDIDOS_ROTA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Roteirizacao limitada a 8 pedidos por execucao."
            );
        }

        final List<PedidoEntity> loaded = pedidoRepository.buscarPorIdsComCliente(
                pedidoIds
        );
        final Map<Long, PedidoEntity> byId = loaded.stream()
                .collect(Collectors.toMap(PedidoEntity::getId,
                        Function.identity()));

        final List<Long> missing = pedidoIds.stream()
                .filter(id -> !byId.containsKey(id))
                .toList();
        if (!missing.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Pedidos nao encontrados: " + missing
            );
        }

        final List<PedidoEntity> orderedPedidos = new ArrayList<>();
        final List<DeliveryRouteService.DeliveryStopInput> inputs =
                new ArrayList<>();
        boolean changed = false;
        for (Long id : pedidoIds) {
            final PedidoEntity pedido = byId.get(id);
            orderedPedidos.add(pedido);

            if (isBlank(pedido.getCodigoEntrega())) {
                pedido.setCodigoEntrega(DeliveryCodeGenerator.nextCode());
                pedido.setCodigoEntregaGeradoEm(LocalDateTime.now());
                changed = true;
            }

            String endereco = normalizeAddress(pedido.getEnderecoEntrega());
            if (endereco.isBlank()) {
                endereco = resolveEnderecoFromUsuario(pedido);
                if (!endereco.isBlank()) {
                    pedido.setEnderecoEntrega(endereco);
                    changed = true;
                }
            }
            if (endereco.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Pedido #" + pedido.getId()
                        + " nao possui endereco de entrega."
                );
            }

            inputs.add(
                    new DeliveryRouteService.DeliveryStopInput(
                            pedido.getId(),
                            pedido.getCliente() != null
                                    ? pedido.getCliente().getNome()
                                    : "Cliente",
                            endereco,
                            pedido.getCodigoEntrega(),
                            pedido.getStatus() != null
                                    ? pedido.getStatus().name()
                                    : "DESCONHECIDO"
                    )
            );
        }
        if (changed) {
            pedidoRepository.saveAll(orderedPedidos);
        }

        final String origem = !isBlank(request.origem())
                ? request.origem().trim()
                : appProps.getAddressQuery();
        final DeliveryRouteService.PlannedRoute planned =
                deliveryRouteService.plan(origem, inputs);
        return new RoteirizacaoResponse(
                planned.origem(),
                planned.distanciaTotalKm(),
                planned.paradas(),
                planned.mapaUrl()
        );
    }

    @PostMapping("/{pedidoId}/confirmar")
    @Transactional
    public ConfirmarEntregaResponse confirmarEntrega(
            @PathVariable("pedidoId") final Long pedidoId,
            @Valid @RequestBody final ConfirmarEntregaRequest request
    ) {
        final PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido nao encontrado."
                ));
        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pedido cancelado nao pode ser confirmado."
            );
        }

        final String expected = normalizeCode(pedido.getCodigoEntrega());
        if (expected.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pedido sem codigo de entrega. Gere um novo codigo."
            );
        }
        final String informed = normalizeCode(request.codigo());
        if (!expected.equals(informed)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Codigo de entrega invalido."
            );
        }

        if (pedido.getCodigoEntregaConfirmadoEm() == null) {
            pedido.setCodigoEntregaConfirmadoEm(LocalDateTime.now());
        }
        pedido.setStatus(StatusPedido.ENTREGUE);
        pedidoRepository.save(pedido);

        return new ConfirmarEntregaResponse(
                pedido.getId(),
                pedido.getStatus().name(),
                pedido.getCodigoEntregaConfirmadoEm()
        );
    }

    @PostMapping("/{pedidoId}/codigo/regenerar")
    @Transactional
    public CodigoEntregaResponse regenerarCodigo(
            @PathVariable("pedidoId") final Long pedidoId
    ) {
        final PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido nao encontrado."
                ));
        if (pedido.getStatus() == StatusPedido.ENTREGUE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pedido ja entregue. Nao e permitido regenerar codigo."
            );
        }

        pedido.setCodigoEntrega(DeliveryCodeGenerator.nextCode());
        pedido.setCodigoEntregaGeradoEm(LocalDateTime.now());
        pedido.setCodigoEntregaConfirmadoEm(null);
        pedidoRepository.save(pedido);

        return new CodigoEntregaResponse(
                pedido.getId(),
                pedido.getCodigoEntrega(),
                pedido.getCodigoEntregaGeradoEm()
        );
    }

    private String resolveEnderecoFromUsuario(final PedidoEntity pedido) {
        if (pedido.getCliente() == null || isBlank(pedido.getCliente().getEmail())) {
            return "";
        }
        return usuarioRepository
                .findByEmailIgnoreCase(pedido.getCliente().getEmail())
                .map(UsuarioEntity::getEndereco)
                .map(this::normalizeAddress)
                .orElse("");
    }

    private static List<Long> uniqueIds(final List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private String normalizeAddress(final String value) {
        if (value == null) {
            return "";
        }
        final String normalized = value.trim();
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    private static String normalizeCode(final String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    public record RoteirizarRequest(
            @NotEmpty List<@NotNull Long> pedidoIds,
            @Size(max = 255) String origem
    ) {
    }

    public record RoteirizacaoResponse(
            String origem,
            java.math.BigDecimal distanciaTotalKm,
            List<DeliveryRouteService.DeliveryStopPlan> paradas,
            String mapaUrl
    ) {
    }

    public record ConfirmarEntregaRequest(
            @NotBlank @Pattern(regexp = "\\d{6}") String codigo
    ) {
    }

    public record ConfirmarEntregaResponse(
            Long pedidoId,
            String status,
            LocalDateTime confirmadoEm
    ) {
    }

    public record CodigoEntregaResponse(
            Long pedidoId,
            String codigoEntrega,
            LocalDateTime geradoEm
    ) {
    }
}

