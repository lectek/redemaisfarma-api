/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.scheduling.annotation.EnableScheduling
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.domain.sync;

import br.com.redemaisfarma.domain.sync.CheckpointService;
import br.com.redemaisfarma.domain.sync.ImportProdutosService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(prefix="sync.firebird", name={"enabled"}, havingValue="true", matchIfMissing=true)
public class ImportProdutosScheduler {
    private static final Logger log = LoggerFactory.getLogger(ImportProdutosScheduler.class);
    private static final String SOURCE = "produtos";
    private final CheckpointService checkpoint;
    private final ImportProdutosService service;
    @Value(value="${sync.firebird.batch-size:1000}")
    private int batchSize;

    public ImportProdutosScheduler(CheckpointService checkpoint, ImportProdutosService service) {
        this.checkpoint = checkpoint;
        this.service = service;
    }

    @Scheduled(fixedDelayString="${sync.firebird.period-ms:300000}")
    public void run() {
        try {
            LocalDateTime since = this.checkpoint.readSince(SOURCE, LocalDateTime.of(1900, 1, 1, 0, 0));
            LocalDateTime newSince = this.service.importarLote(since, this.batchSize);
            if (newSince.isAfter(since)) {
                this.checkpoint.writeSince(SOURCE, newSince);
                log.info("Import produtos avan\u00e7ou checkpoint: {} -> {}", (Object)since, (Object)newSince);
            } else {
                log.debug("Import produtos sem avan\u00e7os (since={})", (Object)since);
            }
        }
        catch (Exception e) {
            log.error("Erro no import de produtos", (Throwable)e);
        }
    }
}

