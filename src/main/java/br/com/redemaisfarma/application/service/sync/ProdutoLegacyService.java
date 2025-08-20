package br.com.redemaisfarma.application.service.sync;

import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.application.dto.legacy.ProdutoLegacyDTO;
import br.com.redemaisfarma.application.mapper.ProdutoLegacyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoLegacyService {

    private final ProdutoLegacyRepository produtoLegacyRepository;
    private final ProdutoLegacyMapper produtoLegacyMapper;

    public List<ProdutoLegacyDTO> buscarTodosComEstoque() {
        List<ProdutoLegacyEntity> produtos = produtoLegacyRepository.findBySaldoGreaterThan(0f);
        return produtos.stream().map(produtoLegacyMapper::toDto).toList();
    }

    public Optional<ProdutoLegacyDTO> buscarPorId(Integer id) {
        return produtoLegacyRepository.findById(id).map(produtoLegacyMapper::toDto);
    }

    public Optional<ProdutoLegacyDTO> buscarPorCodigoBarras(String codigoBarras) {
        return produtoLegacyRepository.findByCodigoBarras(codigoBarras).map(produtoLegacyMapper::toDto);
    }

    public List<ProdutoLegacyDTO> buscarPorNomeParcial(String nome) {
        return produtoLegacyRepository.findByNomeContainingIgnoreCase(nome).stream().map(produtoLegacyMapper::toDto)
                .toList();
    }
}
