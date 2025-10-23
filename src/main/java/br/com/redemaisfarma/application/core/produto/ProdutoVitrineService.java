package br.com.redemaisfarma.application.core.produto;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoVitrineService {

    private final ProdutoRepository repo;

    public ProdutoVitrineService(ProdutoRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<ProdutoEntity> listarDestaques(int limit) {
        var page = PageRequest.of(0, Math.max(1, Math.min(limit, 20)));
        var destaques = repo.findCarrossel(page);
        return destaques.isEmpty() ? repo.findVitrineFallback(page) : destaques;
    }
}
