/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.domain.sync;

import br.com.redemaisfarma.adapters.outbound.legacy.dto.LegacyProdutoDTO;
import br.com.redemaisfarma.adapters.outbound.legacy.port.LegacyProdutoPort;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.domain.sync.CheckpointService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="sync.firebird", name={"enabled"}, havingValue="true", matchIfMissing=true)
public class SyncScheduler {
    private static final Logger log = LoggerFactory.getLogger(SyncScheduler.class);
    private static final String SOURCE = "firebird.produtos";
    private final LegacyProdutoPort legacy;
    private final ProdutoRepository repo;
    private final CheckpointService checkpoint;
    private final int pageSize = 500;

    public SyncScheduler(LegacyProdutoPort legacy, ProdutoRepository repo, CheckpointService checkpoint) {
        this.legacy = legacy;
        this.repo = repo;
        this.checkpoint = checkpoint;
    }

    @Scheduled(cron="${sync.firebird.cron:0 */5 * * * *}")
    public void run() {
        List<LegacyProdutoDTO> lote;
        LocalDateTime since = this.checkpoint.findSinceOrEpoch(SOURCE);
        log.info("Iniciando sync de produtos desde {}", (Object)since);
        int page = 0;
        LocalDateTime lastSeen = since;
        int totalUpserts = 0;
        do {
            try {
                lote = this.legacy.fetchChangedSince(since, page, 500);
            }
            catch (Exception e) {
                log.error("Falha no fetchChangedSince(page={}): {}", new Object[]{page, e.getMessage(), e});
                break;
            }
            if (lote.isEmpty()) break;
            LocalDateTime maxUpdated = lote.stream().map(LegacyProdutoDTO::updatedAt).max(Comparator.naturalOrder()).orElse(lastSeen);
            for (LegacyProdutoDTO r : lote) {
                if (r.nome() == null || r.precoVenda() == null || r.precoVenda().compareTo(BigDecimal.ZERO) <= 0) continue;
                ProdutoEntity p = this.repo.findByCodigoBarras(r.ean()).orElseGet(ProdutoEntity::new);
                p.setNome(r.nome());
                p.setDescricao(r.apresentacao());
                p.setCodigoBarras(r.ean());
                p.setPrecoVenda(r.precoVenda());
                p.setPrecoPromocional(this.zeroToNull(r.precoPromocional()));
                p.setEstoque(Math.max(0, r.estoque()));
                p.setDisponivel(p.getEstoque() > 0 && p.getPrecoVenda().compareTo(BigDecimal.ZERO) > 0);
                p.setLegacyId(r.legacyId());
                p.setStatusSync("SYNCED");
                if (p.getPrecoPromocional() != null && p.getPrecoPromocional().compareTo(BigDecimal.ZERO) > 0) {
                    double pct = 100.0 * (1.0 - p.getPrecoPromocional().doubleValue() / p.getPrecoVenda().doubleValue());
                    p.setDescontoPercentual((int)Math.round(pct));
                } else {
                    p.setDescontoPercentual(null);
                }
                this.repo.save(p);
                ++totalUpserts;
            }
            lastSeen = maxUpdated;
            ++page;
        } while (lote.size() >= 500);
        if (lastSeen.isAfter(since)) {
            this.checkpoint.writeSince(SOURCE, lastSeen);
        }
        log.info("Sync de produtos finalizado. upserts={}, since={} -> {}", new Object[]{totalUpserts, since, lastSeen});
    }

    private BigDecimal zeroToNull(BigDecimal v) {
        return v == null || v.compareTo(BigDecimal.ZERO) <= 0 ? null : v;
    }
}

