package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!test")
public class AdminMetricsService {

    private final ObjectProvider<ProdutoRepository> produtoRepo;
    private final ObjectProvider<PedidoRepository>  pedidoRepo;
    private final ObjectProvider<ClienteRepository> clienteRepo;

    public AdminMetricsService(ObjectProvider<ProdutoRepository> produtoRepo,
                               ObjectProvider<PedidoRepository> pedidoRepo,
                               ObjectProvider<ClienteRepository> clienteRepo) {
        this.produtoRepo = produtoRepo;
        this.pedidoRepo  = pedidoRepo;
        this.clienteRepo = clienteRepo;
    }

    @Transactional(readOnly = true)
    public PainelAdminResponseDTO montarPainel() {
        var p = new PainelAdminResponseDTO();
        p.setAdminNome("Alex Morais");

        // contadores principais (com fallback simples)
        p.setQtdProdutos(produtoRepo.getIfAvailable() != null ? produtoRepo.getIfAvailable().count() : 250L);
        p.setTotalPedidos(pedidoRepo.getIfAvailable()  != null ? pedidoRepo.getIfAvailable().count()  : 350L);
        p.setClientesAtivos(clienteRepo.getIfAvailable()!= null ? clienteRepo.getIfAvailable().count(): 120L);

        // pendentes x entregues
        if (pedidoRepo.getIfAvailable() != null) {
            var pr = pedidoRepo.getIfAvailable();
            p.setQtdPedidosPendentes(pr.countByStatus(StatusPedido.AGUARDANDO_PAGAMENTO));
            p.setQtdPedidosEntregues(pr.countByStatus(StatusPedido.ENTREGUE));

            // mapa por status (para gráficos)
            Map<StatusPedido, Long> porStatus = new EnumMap<>(StatusPedido.class);
            for (StatusPedido s : StatusPedido.values()) {
                porStatus.put(s, pr.countByStatus(s));
            }
            p.setPedidosPorStatus(porStatus);
        } else {
            // fallback mock
            Map<StatusPedido, Long> porStatus = new EnumMap<>(StatusPedido.class);
            porStatus.put(StatusPedido.ABERTO, 40L);
            porStatus.put(StatusPedido.AGUARDANDO_PAGAMENTO, 12L);
            porStatus.put(StatusPedido.PAGO, 90L);
            porStatus.put(StatusPedido.ENVIADO, 25L);
            porStatus.put(StatusPedido.ENTREGUE, 121L);
            porStatus.put(StatusPedido.CANCELADO, 5L);
            p.setPedidosPorStatus(porStatus);
            p.setQtdPedidosPendentes(porStatus.get(StatusPedido.AGUARDANDO_PAGAMENTO));
            p.setQtdPedidosEntregues(porStatus.get(StatusPedido.ENTREGUE));
        }

        // demais métricas (ainda mock)
        p.setTotalLucro(18452.75);
        p.setTicketMedio(124.75);
        p.setSatisfacaoCliente(92.3);
        p.setCategoriasMaisVendidas(List.of("Rações", "Coleiras", "Brinquedos"));
        p.setDataUltimoPedido(LocalDateTime.now().minusHours(2));
        p.setAlertas(List.of(
                "12 pedidos aguardando envio",
                "3 produtos com estoque abaixo de 5 unidades",
                "Novo cliente VIP cadastrado hoje"
        ));

        return p;
    }
}
