package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.domain.support.BarcodeNormalizer;
import br.com.redemaisfarma.domain.support.ProdutoHashUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstoqueFisicoImportService {

    private static final int BATCH_SIZE = 400;

    private final EstoqueFisicoCsvService estoqueFisicoCsvService;
    private final ProdutoRepository produtoRepository;

    @PersistenceContext(unitName = "mysqlPU")
    private EntityManager em;

    @Transactional(transactionManager = "mysqlTransactionManager")
    public ImportacaoResumo importarTodosComoNaoDisponiveis() {
        List<EstoqueFisicoCsvService.EstoqueItem> itens = this.estoqueFisicoCsvService.search("");
        if (itens.isEmpty()) {
            return new ImportacaoResumo(0, 0, 0, 0, 0);
        }

        int lidos = 0;
        int inseridos = 0;
        int atualizados = 0;
        int ignorados = 0;
        int erros = 0;

        List<ProdutoEntity> lote = new ArrayList<>(BATCH_SIZE);
        Map<Long, ProdutoEntity> cacheLegacy = new LinkedHashMap<>();
        Map<String, ProdutoEntity> cacheBarcode = new LinkedHashMap<>();

        for (EstoqueFisicoCsvService.EstoqueItem item : itens) {
            lidos++;
            try {
                ProdutoEntity entity = this.resolveProdutoExistente(item, cacheLegacy, cacheBarcode);
                boolean novo = entity.getId() == null;
                Snapshot before = novo ? null : Snapshot.from(entity);

                this.applyCsvData(entity, item);
                Snapshot after = Snapshot.from(entity);

                if (!novo && Objects.equals(before, after)) {
                    ignorados++;
                    continue;
                }

                lote.add(entity);
                if (novo) {
                    inseridos++;
                } else {
                    atualizados++;
                }

                if (lote.size() >= BATCH_SIZE) {
                    this.flushBatch(lote, cacheLegacy, cacheBarcode);
                }
            } catch (Exception ex) {
                erros++;
                log.warn("[estoque-import] falha no item legacyId={} codigo={} nome='{}': {}",
                        item.legacyId(), item.codigoBarras(), item.nome(), ex.getMessage());
            }
        }

        this.flushBatch(lote, cacheLegacy, cacheBarcode);
        return new ImportacaoResumo(lidos, inseridos, atualizados, ignorados, erros);
    }

    private ProdutoEntity resolveProdutoExistente(
            EstoqueFisicoCsvService.EstoqueItem item,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        Long legacyId = item.legacyId();
        String barcode = BarcodeNormalizer.normalize(item.codigoBarras());

        ProdutoEntity byLegacy = this.resolveByLegacyId(legacyId, barcode, cacheLegacy, cacheBarcode);
        if (byLegacy != null) {
            return byLegacy;
        }

        ProdutoEntity byBarcode = this.resolveByBarcode(barcode, legacyId, cacheLegacy, cacheBarcode);
        if (byBarcode != null) {
            return byBarcode;
        }

        return new ProdutoEntity();
    }

    private ProdutoEntity resolveByLegacyId(
            Long legacyId,
            String barcode,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        if (legacyId == null) {
            return null;
        }

        ProdutoEntity cached = cacheLegacy.get(legacyId);
        if (cached != null) {
            return cached;
        }

        ProdutoEntity found = this.produtoRepository.findByLegacyId(legacyId).orElse(null);
        if (found == null) {
            return null;
        }

        cacheLegacy.put(legacyId, found);
        if (!barcode.isBlank()) {
            cacheBarcode.putIfAbsent(barcode, found);
        }
        return found;
    }

    private ProdutoEntity resolveByBarcode(
            String barcode,
            Long legacyId,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        if (barcode.isBlank()) {
            return null;
        }

        ProdutoEntity cached = cacheBarcode.get(barcode);
        if (cached != null) {
            return cached;
        }

        ProdutoEntity found = this.produtoRepository.findByCodigoBarras(barcode).orElse(null);
        if (found == null) {
            return null;
        }

        cacheBarcode.put(barcode, found);
        if (legacyId != null) {
            cacheLegacy.putIfAbsent(legacyId, found);
        }
        return found;
    }

    private void applyCsvData(ProdutoEntity entity, EstoqueFisicoCsvService.EstoqueItem item) {
        String barcode = BarcodeNormalizer.normalize(item.codigoBarras());
        Long legacyId = item.legacyId();
        String nome = truncate(defaultText(item.nome(), "Produto do estoque fisico"), 255);
        String descricao = truncate(defaultText(item.nome(), "Produto do estoque fisico"), 1000);
        String fabricante = truncate(blankToNull(item.fabricante()), 128);

        BigDecimal precoVenda = this.resolvePrecoVenda(item);
        BigDecimal precoCusto = this.resolvePrecoCusto(item, precoVenda);
        Integer estoque = item.estoque() == null ? 0 : Math.max(0, item.estoque());

        entity.setLegacyId(legacyId);
        entity.setCodigoBarras(barcode.isBlank() ? null : truncate(barcode, 64));
        entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        entity.setNome(nome);
        entity.setDescricao(descricao);
        entity.setCategoria("Estoque fisico");
        entity.setFabricante(fabricante);
        entity.setEstoque(estoque);
        entity.setPrecoVenda(precoVenda);
        entity.setPrecoCusto(precoCusto);
        entity.setDisponivel(Boolean.FALSE);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setPublicadoEm(null);
        entity.setDataImportacao(LocalDateTime.now());
        entity.setStatusSync("SINCRONIZADO");
        if (entity.getDataCadastro() == null) {
            entity.setDataCadastro(LocalDate.now());
        }

        String hash = ProdutoHashUtil.buildHash(
                entity.getCodigoBarras() == null ? "" : entity.getCodigoBarras(),
                entity.getNome() == null ? "" : entity.getNome(),
                entity.getDescricao() == null ? "" : entity.getDescricao(),
                entity.getPrecoVenda(),
                entity.getLegacyId()
        );
        entity.setHashLegado(hash);
    }

    private BigDecimal resolvePrecoVenda(EstoqueFisicoCsvService.EstoqueItem item) {
        BigDecimal venda = item.precoVenda();
        if (venda != null && venda.compareTo(BigDecimal.ZERO) > 0) {
            return venda.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal tabela = item.precoTabela();
        if (tabela != null && tabela.compareTo(BigDecimal.ZERO) > 0) {
            return tabela.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolvePrecoCusto(EstoqueFisicoCsvService.EstoqueItem item, BigDecimal fallback) {
        BigDecimal tabela = item.precoTabela();
        if (tabela != null && tabela.compareTo(BigDecimal.ZERO) >= 0) {
            return tabela.setScale(2, RoundingMode.HALF_UP);
        }
        if (fallback == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return fallback.setScale(2, RoundingMode.HALF_UP);
    }

    private void flushBatch(
            List<ProdutoEntity> lote,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        if (lote.isEmpty()) {
            return;
        }
        List<ProdutoEntity> saved = this.produtoRepository.saveAll(lote);
        for (ProdutoEntity entity : saved) {
            if (entity.getLegacyId() != null) {
                cacheLegacy.put(entity.getLegacyId(), entity);
            }
            String barcode = BarcodeNormalizer.normalize(entity.getCodigoBarras());
            if (!barcode.isBlank()) {
                cacheBarcode.put(barcode, entity);
            }
        }
        lote.clear();
        this.em.flush();
        this.em.clear();
    }

    private static String defaultText(String value, String fallback) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isBlank() ? fallback : trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }

    private record Snapshot(
            Long legacyId,
            String codigoBarras,
            String nome,
            String descricao,
            String categoria,
            String fabricante,
            Integer estoque,
            BigDecimal precoVenda,
            BigDecimal precoCusto,
            Boolean disponivel,
            ProdutoStatus status
    ) {
        static Snapshot from(ProdutoEntity p) {
            return new Snapshot(
                    p.getLegacyId(),
                    BarcodeNormalizer.normalizeOrNull(p.getCodigoBarras()),
                    p.getNome(),
                    p.getDescricao(),
                    p.getCategoria(),
                    p.getFabricante(),
                    p.getEstoque(),
                    p.getPrecoVenda(),
                    p.getPrecoCusto(),
                    p.getDisponivel(),
                    p.getStatus()
            );
        }
    }

    public record ImportacaoResumo(
            int lidos,
            int inseridos,
            int atualizados,
            int ignorados,
            int erros
    ) {
    }
}
