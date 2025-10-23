package br.com.redemaisfarma.adapters.outbound.legacy.port;

import br.com.redemaisfarma.adapters.outbound.legacy.dto.LegacyProdutoDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Porta de saída (Outbound Port) responsável por buscar produtos do sistema Firebird.
 */
public interface LegacyProdutoPort {

    /**
     * Busca produtos alterados desde um determinado timestamp.
     * Pagina por offset para não sobrecarregar a conexão.
     */
    List<LegacyProdutoDTO> fetchChangedSince(LocalDateTime since, int page, int size);
}
