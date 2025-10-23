package br.com.redemaisfarma.infrastructure.financeiro;

import br.com.redemaisfarma.application.view.AssinaturaView;
import br.com.redemaisfarma.domain.financeiro.FinanceiroAdminService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Mock simples para DEV/DOCKER. Em produção substitua por implementação que
 * consulta o banco/repositório real.
 */
@Service
@Profile({"default", "docker", "dev"})
public class InMemoryFinanceiroAdminService implements FinanceiroAdminService {

    private final Map<Long, AssinaturaView> db = new LinkedHashMap<>();

    public InMemoryFinanceiroAdminService() {
        db.put(1L, new AssinaturaView(1L, "Fulano da Silva", "Plano VIP",
                new BigDecimal("149.90"), LocalDate.now().plusDays(12), "ATIVA"));
        db.put(2L, new AssinaturaView(2L, "Maria Souza", "Plano Basic",
                new BigDecimal("49.90"), LocalDate.now().plusDays(3), "ATIVA"));
        db.put(3L, new AssinaturaView(3L, "Comércio XPTO", "Plano Pro",
                new BigDecimal("89.90"), LocalDate.now().minusDays(1), "ATRASADA"));
    }

    @Override
    public List<AssinaturaView> listarAssinaturas() {
        return new ArrayList<>(db.values());
    }

    @Override
    public Optional<AssinaturaView> buscarAssinatura(Long id) {
        return Optional.ofNullable(db.get(id));
    }
}
