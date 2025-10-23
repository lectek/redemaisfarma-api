package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageEventPublisher;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImageAutoTriggerService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageAutoTriggerService.class);

    private final ProdutoRepository produtoRepo;
    private final ProductImageEventPublisher publisher;
    private final boolean cronEnabled;
    private final int cronBatchSize;

    public ProductImageAutoTriggerService(ProdutoRepository produtoRepo,
                                          ProductImageEventPublisher publisher,
                                          @Value("${app.ai.image.autogen.cron-enabled:false}") boolean cronEnabled,
                                          @Value("${app.ai.image.autogen.batch-size:25}") int cronBatchSize) {
        this.produtoRepo = produtoRepo;
        this.publisher = publisher;
        this.cronEnabled = cronEnabled;
        this.cronBatchSize = cronBatchSize;
    }

    /** Dispara evento para UM produto específico (se estiver sem imagem). */
    @Transactional(readOnly = true)
    public boolean triggerOne(Long productId) {
        return produtoRepo.findById(productId)
                .filter(this::semImagem)
                .map(p -> { publishEvent(p); return true; })
                .orElse(false);
    }

    /** Escaneia e dispara eventos para um lote de produtos sem mídia. */
    @Transactional(readOnly = true)
    public int triggerBatch(int size) {
        var pageable = PageRequest.of(0, Math.max(1, size));
        var page = produtoRepo.findSemMidia(pageable);
        page.forEach(this::publishEvent);
        return page.getNumberOfElements();
    }

    private boolean semImagem(ProdutoEntity p) {
        String img = p.getImagem();
        return (img == null || img.isBlank());
    }

    private void publishEvent(ProdutoEntity p) {
        // Como ProdutoEntity não tem slug/marca, enviamos vazio (opcional no record).
        var evt = new ProductImageRequestedEvent(
                p.getId(),
                safe(p.getNome()),
                "",                      // marca (preencha se existir na entidade)
                safe(p.getCategoria()),
                ""                       // slug ausente na entidade
        );
        publisher.publish(evt);
        log.info("Publicado pedido de imagem para produto {} ({})", p.getId(), p.getNome());
    }

    private String safe(String s) { return s == null ? "" : s; }

    /** Agendamento opcional: ligar com app.ai.image.autogen.cron-enabled=true */
    @Scheduled(fixedDelayString = "${app.ai.image.autogen.fixed-delay-ms:300000}")
    public void scheduledScan() {
        if (!cronEnabled) return;
        int n = triggerBatch(cronBatchSize);
        if (n > 0) log.info("AutoTrigger: publicados {} pedidos de imagem (sem mídia).", n);
    }
}
