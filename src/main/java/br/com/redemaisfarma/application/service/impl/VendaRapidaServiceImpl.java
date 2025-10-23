package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.application.service.VendaRapidaService;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import br.com.redemaisfarma.domain.service.EstoqueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Profile("!test")
@Service
@RequiredArgsConstructor
public class VendaRapidaServiceImpl implements VendaRapidaService {

    private final ClienteRepository clienteRepo;
    private final ProdutoRepository produtoRepo;
    private final PedidoRepository  pedidoRepo;
    private final EstoqueService estoque;

    @Override
    @Transactional
    public Long criar(String refCliente, String refProduto, int qtd) {
        if (qtd <= 0) throw new IllegalArgumentException("Quantidade inválida.");

        var cliente = buscarCliente(refCliente);
        var produto = buscarProduto(refProduto);

        // getEstoque é int -> não compare com null
        int est = produto.getEstoque();
        if (est < qtd || !estoque.temDisponivel(produto.getId(), qtd)) {
            throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
        }

        var pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setData(LocalDateTime.now());
        pedido.setStatus(StatusPedido.ABERTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setTotal(BigDecimal.ZERO);

        var item = new ItemPedidoEntity();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(qtd);

        BigDecimal preco = produto.getPrecoVenda() != null ? produto.getPrecoVenda() : BigDecimal.ZERO;
        item.setSubtotal(preco.multiply(BigDecimal.valueOf(qtd)));

        pedido.addItem(item);

        BigDecimal total = pedido.getItens().stream()
                .map(ItemPedidoEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);

        pedidoRepo.save(pedido);
        estoque.baixar(produto.getId(), qtd, "Venda rápida pedido #" + pedido.getId());

        return pedido.getId();
    }

    private br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity buscarCliente(String ref) {
        if (ref == null || ref.isBlank())
            throw new IllegalArgumentException("Informe o cliente.");

        return clienteRepo.findByEmailIgnoreCase(ref)
                .or(() -> clienteRepo.findByCpf(ref))
                .or(() -> clienteRepo.findFirstByNomeContainingIgnoreCase(ref))
                .or(() -> clienteRepo.findByEmail(ref))
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado."));
    }

    private ProdutoEntity buscarProduto(String ref) {
        if (ref == null || ref.isBlank())
            throw new IllegalArgumentException("Informe o produto.");

        return produtoRepo.findByCodigoBarras(ref)
                .or(() -> produtoRepo.findFirstByNomeContainingIgnoreCase(ref))
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));
    }
}
