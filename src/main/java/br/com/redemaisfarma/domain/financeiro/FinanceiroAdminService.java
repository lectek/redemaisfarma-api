package br.com.redemaisfarma.domain.financeiro;

import java.util.List;
import java.util.Optional;

import br.com.redemaisfarma.application.view.AssinaturaView;

public interface FinanceiroAdminService {
    List<AssinaturaView> listarAssinaturas();
    Optional<AssinaturaView> buscarAssinatura(Long id);
}
