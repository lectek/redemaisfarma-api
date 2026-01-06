package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.view.CartItemVM;
import br.com.redemaisfarma.application.view.CartSummaryVM;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private static final String SESSION_CART = "cartItems";

    private final ProdutoRepository produtoRepository;

    public CartService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public void addItem(HttpSession session, Long produtoId, int quantidade) {
        if (produtoId == null || quantidade <= 0) return;
        List<CartSessionItem> items = getSessionItems(session);
        for (CartSessionItem item : items) {
            if (produtoId.equals(item.produtoId())) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                session.setAttribute(SESSION_CART, items);
                return;
            }
        }
        items.add(new CartSessionItem(produtoId, quantidade));
        session.setAttribute(SESSION_CART, items);
    }

    public CartValidationResult validateAdd(Long produtoId, int quantidade) {
        if (produtoId == null) {
            return new CartValidationResult(false, "Produto invalido.");
        }
        if (quantidade <= 0) {
            return new CartValidationResult(false, "Quantidade invalida.");
        }
        ProdutoEntity produto = produtoRepository.findById(produtoId).orElse(null);
        if (produto == null) {
            return new CartValidationResult(false, "Produto nao encontrado.");
        }
        CartIssue issue = validateProduto(produto, quantidade);
        if (issue.invalid) {
            return new CartValidationResult(false, issue.message);
        }
        return new CartValidationResult(true, "");
    }

    public void updateItem(HttpSession session, Long produtoId, int quantidade) {
        if (produtoId == null) return;
        List<CartSessionItem> items = getSessionItems(session);
        items.removeIf(item -> produtoId.equals(item.produtoId()) && quantidade <= 0);
        for (CartSessionItem item : items) {
            if (produtoId.equals(item.produtoId())) {
                item.setQuantidade(quantidade);
                session.setAttribute(SESSION_CART, items);
                return;
            }
        }
        if (quantidade > 0) {
            items.add(new CartSessionItem(produtoId, quantidade));
        }
        session.setAttribute(SESSION_CART, items);
    }

    public void removeItem(HttpSession session, Long produtoId) {
        if (produtoId == null) return;
        List<CartSessionItem> items = getSessionItems(session);
        items.removeIf(item -> produtoId.equals(item.produtoId()));
        session.setAttribute(SESSION_CART, items);
    }

    public CartSummaryVM buildSummary(HttpSession session) {
        List<CartSessionItem> items = getSessionItems(session);
        if (items.isEmpty()) {
            return new CartSummaryVM(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, false);
        }
        Map<Long, ProdutoEntity> produtos = loadProdutos(items);
        List<CartItemVM> viewItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        boolean hasInvalid = false;
        for (CartSessionItem item : items) {
            ProdutoEntity produto = produtos.get(item.produtoId());
            if (produto == null) {
                continue;
            }
            BigDecimal preco = produto.getPrecoVenda() == null ? BigDecimal.ZERO : produto.getPrecoVenda();
            BigDecimal sub = preco.multiply(BigDecimal.valueOf(item.getQuantidade()));
            CartIssue issue = validateProduto(produto, item.getQuantidade());
            if (issue.invalid) {
                hasInvalid = true;
            } else {
                subtotal = subtotal.add(sub);
            }
            viewItems.add(new CartItemVM(
                    produto.getId(),
                    produto.getNome(),
                    produto.getImagem(),
                    preco,
                    item.getQuantidade(),
                    sub,
                    issue.invalid,
                    issue.message,
                    produto.getEstoque()
            ));
        }
        return new CartSummaryVM(viewItems, subtotal, subtotal, hasInvalid);
    }

    public CartOrderData buildOrderData(HttpSession session) {
        List<CartSessionItem> items = getSessionItems(session);
        if (items.isEmpty()) {
            return new CartOrderData(List.of(), BigDecimal.ZERO, List.of());
        }
        Map<Long, ProdutoEntity> produtos = loadProdutos(items);
        List<ItemPedidoEntity> orderItems = new ArrayList<>();
        List<CartItemVM> invalidItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartSessionItem item : items) {
            ProdutoEntity produto = produtos.get(item.produtoId());
            if (produto == null) {
                continue;
            }
            BigDecimal preco = produto.getPrecoVenda() == null ? BigDecimal.ZERO : produto.getPrecoVenda();
            BigDecimal sub = preco.multiply(BigDecimal.valueOf(item.getQuantidade()));
            CartIssue issue = validateProduto(produto, item.getQuantidade());
            if (issue.invalid) {
                invalidItems.add(new CartItemVM(
                        produto.getId(),
                        produto.getNome(),
                        produto.getImagem(),
                        preco,
                        item.getQuantidade(),
                        sub,
                        true,
                        issue.message,
                        produto.getEstoque()
                ));
                continue;
            }
            ItemPedidoEntity entity = new ItemPedidoEntity();
            entity.setProduto(produto);
            entity.setQuantidade(item.getQuantidade());
            entity.setSubtotal(sub);
            orderItems.add(entity);
            total = total.add(sub);
        }
        return new CartOrderData(orderItems, total, invalidItems);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_CART);
    }

    private Map<Long, ProdutoEntity> loadProdutos(List<CartSessionItem> items) {
        List<Long> ids = items.stream().map(CartSessionItem::produtoId).distinct().toList();
        Map<Long, ProdutoEntity> out = new HashMap<>();
        for (ProdutoEntity produto : produtoRepository.findAllByIdIn(ids)) {
            out.put(produto.getId(), produto);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<CartSessionItem> getSessionItems(HttpSession session) {
        if (session == null) {
            return new ArrayList<>();
        }
        Object value = session.getAttribute(SESSION_CART);
        if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof CartSessionItem) {
            return (List<CartSessionItem>) value;
        }
        if (value instanceof List<?> list && list.isEmpty()) {
            return (List<CartSessionItem>) list;
        }
        return new ArrayList<>();
    }

    public static class CartOrderData {
        private final List<ItemPedidoEntity> items;
        private final BigDecimal total;
        private final List<CartItemVM> invalidItems;

        public CartOrderData(List<ItemPedidoEntity> items, BigDecimal total, List<CartItemVM> invalidItems) {
            this.items = items;
            this.total = total;
            this.invalidItems = invalidItems;
        }

        public List<ItemPedidoEntity> getItems() {
            return items;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public List<CartItemVM> getInvalidItems() {
            return invalidItems;
        }
    }

    public record CartValidationResult(boolean valid, String message) {
    }

    public static class CartSessionItem {
        private Long produtoId;
        private Integer quantidade;

        public CartSessionItem() {
        }

        public CartSessionItem(Long produtoId, Integer quantidade) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
        }

        public Long produtoId() {
            return produtoId;
        }

        public Integer getQuantidade() {
            return quantidade == null ? 0 : quantidade;
        }

        public void setQuantidade(Integer quantidade) {
            this.quantidade = quantidade;
        }
    }

    private static class CartIssue {
        private final boolean invalid;
        private final String message;

        private CartIssue(boolean invalid, String message) {
            this.invalid = invalid;
            this.message = message;
        }
    }

    private CartIssue validateProduto(ProdutoEntity produto, Integer quantidade) {
        if (produto.getDisponivel() != null && !produto.getDisponivel()) {
            return new CartIssue(true, "Indisponivel no momento.");
        }
        Integer estoque = produto.getEstoque();
        if (estoque != null && estoque <= 0) {
            return new CartIssue(true, "Sem estoque.");
        }
        if (estoque != null && quantidade != null && quantidade > estoque) {
            return new CartIssue(true, "Quantidade maior que estoque.");
        }
        BigDecimal preco = produto.getPrecoVenda();
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            return new CartIssue(true, "Preco indisponivel.");
        }
        return new CartIssue(false, "");
    }
}
