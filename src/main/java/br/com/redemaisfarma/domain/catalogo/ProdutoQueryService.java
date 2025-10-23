// src/main/java/br/com/redemaisfarma/domain/catalogo/ProdutoQueryService.java
package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.application.view.ProductCardVM;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Facade de consultas para montar cards/carrossel da Home. */
public interface ProdutoQueryService {

    /** “Mais vendidos” (proxy/fallback). */
    List<ProductCardVM> topSellers(Pageable page, boolean incluirIndisponiveis);

    /** “Novidades”. */
    List<ProductCardVM> newArrivals(Pageable page, boolean incluirIndisponiveis);

    /** “Destaques” (carrossel). */
    List<ProductCardVM> featured(Pageable page, boolean incluirIndisponiveis);

    /** “Para você” (recomendados). */
    List<ProductCardVM> recommended(Pageable page, boolean incluirIndisponiveis);

    /** Atalho usado pelo controller público (/destaques?limit=10). */
    default List<ProductCardVM> featured(int limit) {
        int size = Math.max(1, Math.min(limit, 50)); // limite de segurança
        Pageable page = PageRequest.of(0, size);
        // por padrão NÃO inclui indisponíveis
        return featured(page, false);
    }
}
