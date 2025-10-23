// src/main/java/br/com/redemaisfarma/domain/catalogo/VendasAggregRepository.java
package br.com.redemaisfarma.domain.catalogo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface VendasAggregRepository extends Repository<Object, Long> {

    @Query("""
        select ip.produto.id as id
        from ItemPedido ip
        group by ip.produto.id
        order by sum(ip.quantidade) desc
    """)
    List<Long> topProdutoIds(Pageable page);
}
