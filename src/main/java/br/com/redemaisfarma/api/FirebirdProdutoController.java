// src/main/java/br/com/redemaisfarma/api/FirebirdProdutoController.java
package br.com.redemaisfarma.api;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/firebird")
@ConditionalOnProperty(prefix = "app.sync.legacy", name = "enabled", havingValue = "true")
public class FirebirdProdutoController {

    private final ProdutoLegacyRepository repo;

    public FirebirdProdutoController(ProdutoLegacyRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/produtos/codigo-barras/{ean}")
    public ProdutoLegacyEntity porEan(@PathVariable String ean) {
        return repo.findByCodigoBarras(ean).orElse(null);
    }

    @GetMapping("/produtos/busca")
    public List<ProdutoLegacyEntity> porNome(@RequestParam String q) {
        return repo.findByNomeContainingIgnoreCase(q);
    }

    @GetMapping("/produtos/estoque-positivo")
    public List<ProdutoLegacyEntity> estoquePositivo(@RequestParam(defaultValue = "0") BigDecimal minimo) {
        return repo.findBySaldoGreaterThan(minimo);
    }
}
