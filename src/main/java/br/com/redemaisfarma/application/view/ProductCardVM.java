package br.com.redemaisfarma.application.view;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public record ProductCardVM(
        Long id,
        String nome,
        String imagem,
        BigDecimal preco,
        boolean disponivel,
        Integer estoque,
        String categoria
) {

    public static ProductCardVM of(ProdutoEntity p) {
        if (p == null) return null;
        return new ProductCardVM(
                p.getId(),
                nvl(p.getNome(), "Produto"),
                resolveImage(nvl(p.getImagem(), "")),
                safePreco(p),
                safeDisponivel(p),
                p.getEstoque(),
                nvl(p.getCategoria(), "")
        );
    }

    public static List<ProductCardVM> fromList(List<ProdutoEntity> ps) {
        if (ps == null || ps.isEmpty()) return List.of();
        return ps.stream().map(ProductCardVM::of).collect(Collectors.toList());
    }

    public static List<ProductCardVM> fromListInOrder(List<ProdutoEntity> ps, List<Long> orderIds) {
        if (ps == null || ps.isEmpty() || orderIds == null || orderIds.isEmpty()) {
            return fromList(ps);
        }
        final Map<Long, ProductCardVM> map = new HashMap<>();
        ps.forEach(p -> map.put(p.getId(), of(p)));

        final List<ProductCardVM> out = new ArrayList<>();
        for (Long id : orderIds) {
            final ProductCardVM vm = map.get(id);
            if (vm != null) out.add(vm);
        }
        return out;
    }

    public static ProductPageVM fromPage(Page<ProdutoEntity> page) {
        return new ProductPageVM(
                fromList(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private static String nvl(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private static BigDecimal safePreco(ProdutoEntity p) {
        return p.getPrecoVenda() != null ? p.getPrecoVenda() : BigDecimal.ZERO;
    }

    private static boolean safeDisponivel(ProdutoEntity p) {
        return Boolean.TRUE.equals(p.getDisponivel());
    }

    private static String resolveImage(String raw) {
        if (raw == null || raw.isBlank()) return "/images/placeholders/product.png";
        final String r = raw.trim();
        if (r.startsWith("http://") || r.startsWith("https://") || r.startsWith("//")) return r;
        return "/media/products/" + r.replaceFirst("^[/\\\\]+", "");
    }

    public record ProductPageVM(
            List<ProductCardVM> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) { }
}
