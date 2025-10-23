package br.com.redemaisfarma.adapters.outbound.persistence.adapter;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class ProdutoRepositoryAdapter implements ProdutoRepositoryPort {

    private final ProdutoRepository jpa;

    public ProdutoRepositoryAdapter(ProdutoRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<ProdutoDTO> findById(Long id) {
        return jpa.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public void updateImagem(Long id, String imageUrl) {
        ProdutoEntity e = jpa.findById(id).orElseThrow();
        e.setImagem(imageUrl);  // precisa existir setImagem(String) na entidade
        jpa.save(e);
    }

    // ---- Helpers ----

    private ProdutoDTO mapToDto(ProdutoEntity e) {
        return new ProdutoDTO(
            e.getId(),
            e.getDescricao(),     // garantido pelas suas queries
            e.getCategoria(),     // garantido pelas suas queries
            e.getCodigoBarras(),  // garantido pelas suas queries
            e.getImagem()         // usado em findSemMidia
        );
    }

    /** Utilitário opcional: pegar N produtos sem mídia (aproveita seu findSemMidia). */
    public java.util.List<ProdutoEntity> firstNProdutosSemMidia(int limit) {
        var page = jpa.findSemMidia(PageRequest.of(0, Math.max(1, limit)));
        return page.getContent();
    }
}
