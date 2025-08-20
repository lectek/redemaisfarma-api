package br.com.redemaisfarma.application.service.sync;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.legacy.mapper.ProdutoLegacyManualMapper;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoSyncService {

    private final ProdutoLegacyRepository legacyRepository;
    private final ProdutoRepository novoRepository;
    private final ProdutoLegacyManualMapper mapper;

    private static final int PAGE_SIZE = 100;

    /**
     * Sincroniza os produtos do Firebird para o novo banco, em blocos paginados.
     *
     * @return Quantidade total de produtos sincronizados com sucesso.
     */
    public int sincronizarProdutos() {
        int pageNumber = 0;
        int totalSincronizados = 0;
        boolean terminou = false;

        log.info("🔄 Iniciando sincronização de produtos do banco legado...");

        while (!terminou) {
            Page<ProdutoLegacyEntity> pagina = legacyRepository.findAll(PageRequest.of(pageNumber, PAGE_SIZE));

            if (pagina.hasContent()) {
                List<ProdutoEntity> convertidos = new ArrayList<>();

                for (ProdutoLegacyEntity legacy : pagina.getContent()) {
                    try {
                        ProdutoEntity entity = mapper.toEntity(legacy);
                        if (entity != null) {
                            convertidos.add(entity);
                        }
                    } catch (Exception e) {
                        log.warn("❌ Erro ao converter produto legado ID={} -> {}", legacy.getId(), e.getMessage());
                    }
                }

                novoRepository.saveAll(convertidos);
                totalSincronizados += convertidos.size();
                log.info("✅ Página {} sincronizada ({} produtos)", pageNumber + 1, convertidos.size());

                pageNumber++;
            } else {
                terminou = true;
            }
        }

        log.info("🎉 Sincronização finalizada. Total sincronizado: {}", totalSincronizados);
        return totalSincronizados;
    }
}
