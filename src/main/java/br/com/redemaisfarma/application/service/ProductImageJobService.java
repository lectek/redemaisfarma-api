/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO
 *  br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase
 *  br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository
 *  br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository$Job
 *  br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort
 *  br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort$ProdutoDTO
 *  br.com.redemaisfarma.domain.ai.ProductPromptFactory
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort;
import br.com.redemaisfarma.domain.ai.ProductPromptFactory;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImageJobService {
    private static final Logger log = LoggerFactory.getLogger(ProductImageJobService.class);
    private final ProdutoRepositoryPort produtos;
    private final ProductImageJobRepository jobs;
    private final ImageStudioUseCase imageStudio;
    private final ProductPromptFactory promptFactory;

    public ProductImageJobService(ProdutoRepositoryPort produtos, ProductImageJobRepository jobs, ImageStudioUseCase imageStudio, ProductPromptFactory promptFactory) {
        this.produtos = produtos;
        this.jobs = jobs;
        this.imageStudio = imageStudio;
        this.promptFactory = promptFactory;
    }

    @Transactional
    public void process(ProductImageRequestedEvent evt) {
        Long productId = evt.productId();
        Optional opt = this.produtos.findById(productId);
        if (opt.isEmpty()) {
            log.warn("Produto {} n\u00e3o encontrado. Ignorando job.", (Object)productId);
            return;
        }
        ProdutoRepositoryPort.ProdutoDTO p = (ProdutoRepositoryPort.ProdutoDTO)opt.get();
        if (p.imagem() != null && !p.imagem().isBlank()) {
            log.debug("Produto {} j\u00e1 possui imagem. Ignorando gera\u00e7\u00e3o.", (Object)p.id());
            return;
        }
        String preset = "packshot";
        Map vars = this.promptFactory.varsFromProduto(p);
        String fingerprint = this.promptFactory.fingerprint("packshot", vars);
        ProductImageJobRepository.Job job = this.jobs.createQueued(p.id(), fingerprint);
        try {
            this.jobs.markRunning(job.id());
            String prompt = this.promptFactory.promptForProduto(p);
            ImageGenRequestDTO req = new ImageGenRequestDTO("packshot", prompt, vars, null, true, true);
            String resultUrl = this.imageStudio.generateSync(req);
            this.produtos.updateImagem(p.id(), resultUrl);
            this.jobs.markDone(job.id(), resultUrl);
            log.info("Imagem gerada com sucesso para produto {} -> {}", (Object)p.id(), (Object)resultUrl);
        }
        catch (Exception e) {
            this.jobs.markError(job.id(), e.getMessage());
            log.error("Falha ao gerar imagem para produto {}: {}", new Object[]{p.id(), e.getMessage(), e});
        }
    }

    @Transactional
    public void regenerateForced(Long productId) {
        ProdutoRepositoryPort.ProdutoDTO p = (ProdutoRepositoryPort.ProdutoDTO)this.produtos.findById(productId).orElseThrow(() -> new IllegalArgumentException("Produto n\u00e3o encontrado: " + String.valueOf(productId)));
        String preset = "packshot";
        Map vars = this.promptFactory.varsFromProduto(p);
        String fingerprint = this.promptFactory.fingerprint("packshot", vars);
        ProductImageJobRepository.Job job = this.jobs.createQueued(p.id(), fingerprint);
        try {
            this.jobs.markRunning(job.id());
            ImageGenRequestDTO req = new ImageGenRequestDTO("packshot", this.promptFactory.promptForProduto(p), vars, null, true, true);
            String resultUrl = this.imageStudio.generateSync(req);
            this.produtos.updateImagem(p.id(), resultUrl);
            this.jobs.markDone(job.id(), resultUrl);
            log.info("[FORCED] Imagem regenerada para produto {} -> {}", (Object)p.id(), (Object)resultUrl);
        }
        catch (Exception e) {
            this.jobs.markError(job.id(), e.getMessage());
            log.error("[FORCED] Falha ao regenerar imagem para produto {}: {}", new Object[]{p.id(), e.getMessage(), e});
        }
    }
}

