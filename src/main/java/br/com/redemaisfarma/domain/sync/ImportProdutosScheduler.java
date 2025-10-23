package br.com.redemaisfarma.domain.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "sync.firebird", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ImportProdutosScheduler {

    private static final Logger log = LoggerFactory.getLogger(ImportProdutosScheduler.class);
    private static final String SOURCE = "produtos";

    private final CheckpointService checkpoint;
    private final ImportProdutosService service;

    @Value("${sync.firebird.batch-size:1000}")
    private int batchSize;

    public ImportProdutosScheduler(CheckpointService checkpoint,
                                   ImportProdutosService service) {
        this.checkpoint = checkpoint;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${sync.firebird.period-ms:300000}")
    public void run() {
        try {
            LocalDateTime since = checkpoint.readSince(SOURCE, LocalDateTime.of(1900,1,1,0,0));
            LocalDateTime newSince = service.importarLote(since, batchSize);
            if (newSince.isAfter(since)) {
                checkpoint.writeSince(SOURCE, newSince);
                log.info("Import produtos avançou checkpoint: {} -> {}", since, newSince);
            } else {
                log.debug("Import produtos sem avanços (since={})", since);
            }
        } catch (Exception e) {
            log.error("Erro no import de produtos", e);
        }
    }
}
