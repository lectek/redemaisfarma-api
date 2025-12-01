package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort;
import br.com.redemaisfarma.domain.ai.ProductPromptFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class ProductImageJobService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageJobService.class);
    private static final String PRESET = "packshot";

    private final ProdutoRepositoryPort produtos;
    private final ProductImageJobRepository jobs;
    private final ImageStudioUseCase imageStudio;
    private final ProductPromptFactory promptFactory;

    public ProductImageJobService(ProdutoRepositoryPort produtos,
                                  ProductImageJobRepository jobs,
                                  ImageStudioUseCase imageStudio,
                                  ProductPromptFactory promptFactory) {
        this.produtos = produtos;
        this.jobs = jobs;
        this.imageStudio = imageStudio;
        this.promptFactory = promptFactory;
    }

    @Transactional
    public void process(ProductImageRequestedEvent evt) {
        Long productId = evt.productId();
        Optional<ProdutoRepositoryPort.ProdutoDTO> opt = produtos.findById(productId);
        if (opt.isEmpty()) {
            log.warn("Produto {} não encontrado. Ignorando job.", productId);
            return;
        }
        var p = opt.get();

        if (p.imagem() != null && !p.imagem().isBlank()) {
            log.debug("Produto {} já possui imagem. Ignorando geração.", p.id());
            return;
        }

        Map<String, Object> vars = promptFactory.varsFromProduto(p);
        String fingerprint = promptFactory.fingerprint(PRESET, vars);
        ProductImageJobRepository.Job job = jobs.createQueued(p.id(), fingerprint);

        try {
            jobs.markRunning(job.id());

            String prompt = promptFactory.promptForProduto(p);
            ImageGenRequestDTO req = new ImageGenRequestDTO(PRESET, prompt, vars, null, true, true);

            String resultUrl = imageStudio.generateSync(req);
            produtos.updateImagem(p.id(), resultUrl);
            jobs.markDone(job.id(), resultUrl);

            log.info("Imagem gerada com sucesso para produto {} -> {}", p.id(), resultUrl);
        } catch (Exception e) {
            jobs.markError(job.id(), e.getMessage());
            log.error("Falha ao gerar imagem para produto {}: {}", p.id(), e.getMessage(), e);
        }
    }

    @Transactional
    public void regenerateForced(Long productId) {
        var p = produtos.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + productId));

        Map<String, Object> vars = promptFactory.varsFromProduto(p);
        String fingerprint = promptFactory.fingerprint(PRESET, vars);
        ProductImageJobRepository.Job job = jobs.createQueued(p.id(), fingerprint);

        try {
            jobs.markRunning(job.id());
            ImageGenRequestDTO req = new ImageGenRequestDTO(PRESET, promptFactory.promptForProduto(p), vars, null, true, true);

            String resultUrl = imageStudio.generateSync(req);
            produtos.updateImagem(p.id(), resultUrl);
            jobs.markDone(job.id(), resultUrl);

            log.info("[FORCED] Imagem regenerada para produto {} -> {}", p.id(), resultUrl);
        } catch (Exception e) {
            jobs.markError(job.id(), e.getMessage());
            log.error("[FORCED] Falha ao regenerar imagem para produto {}: {}", p.id(), e.getMessage(), e);
        }
    }
}
