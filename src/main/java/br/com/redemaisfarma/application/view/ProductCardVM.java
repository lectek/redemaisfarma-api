/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 */
package br.com.redemaisfarma.application.view;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

public record ProductCardVM(Long id, String nome, String imagem, BigDecimal preco, boolean disponivel, Integer estoque, String categoria) {
    public static ProductCardVM of(ProdutoEntity p) {
        if (p == null) {
            return null;
        }
        return new ProductCardVM(p.getId(), ProductCardVM.nvl(p.getNome(), "Produto"), ProductCardVM.resolveImage(ProductCardVM.nvl(p.getImagem(), "")), ProductCardVM.safePreco(p), ProductCardVM.safeDisponivel(p), p.getEstoque(), ProductCardVM.nvl(p.getCategoria(), ""));
    }

    public static List<ProductCardVM> fromList(List<ProdutoEntity> ps) {
        if (ps == null || ps.isEmpty()) {
            return List.of();
        }
        return ps.stream().map(ProductCardVM::of).collect(Collectors.toList());
    }

    public static List<ProductCardVM> fromListInOrder(List<ProdutoEntity> ps, List<Long> orderIds) {
        if (ps == null || ps.isEmpty() || orderIds == null || orderIds.isEmpty()) {
            return ProductCardVM.fromList(ps);
        }
        HashMap map = new HashMap();
        ps.forEach(p -> map.put(p.getId(), ProductCardVM.of(p)));
        ArrayList<ProductCardVM> out = new ArrayList<ProductCardVM>();
        for (Long id : orderIds) {
            ProductCardVM vm = (ProductCardVM)map.get(id);
            if (vm == null) continue;
            out.add(vm);
        }
        return out;
    }

    public static ProductPageVM fromPage(Page<ProdutoEntity> page) {
        return new ProductPageVM(ProductCardVM.fromList(page.getContent()), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }

    private static String nvl(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    private static BigDecimal safePreco(ProdutoEntity p) {
        return p.getPrecoVenda() != null ? p.getPrecoVenda() : BigDecimal.ZERO;
    }

    private static boolean safeDisponivel(ProdutoEntity p) {
        return Boolean.TRUE.equals(p.getDisponivel());
    }

    private static String resolveImage(String raw) {
        if (raw == null || raw.isBlank()) {
            return "/images/placeholders/product.png";
        }
        String r = raw.trim();
        if (r.startsWith("http://") || r.startsWith("https://") || r.startsWith("//")) {
            return r;
        }
        return "/media/products/" + r.replaceFirst("^[/\\\\]+", "");
    }

    public record ProductPageVM(List<ProductCardVM> content, int pageNumber, int pageSize, long totalElements, int totalPages, boolean first, boolean last) {
    }
}

