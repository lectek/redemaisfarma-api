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

@Service
public class ProductImageJobService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageJobService.class);

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

    /**
     * Processa um pedido de geração de imagem recebido via Kafka.
     * Busca o produto pelo ID, gera a imagem (packshot) e atualiza produtos.imagem.
     */
    @Transactional
    public void process(ProductImageRequestedEvent evt) {
        Long productId = evt.productId();
        var opt = produtos.findById(productId);
        if (opt.isEmpty()) {
            log.warn("Produto {} não encontrado. Ignorando job.", productId);
            return;
        }

        var p = opt.get();

        // Se já tem imagem, não reprocessa (use um endpoint de "regenerar" se precisar forçar).
        if (p.imagem() != null && !p.imagem().isBlank()) {
            log.debug("Produto {} já possui imagem. Ignorando geração.", p.id());
            return;
        }

        final String preset = "packshot";
        Map<String, Object> vars = promptFactory.varsFromProduto(p);
        String fingerprint = promptFactory.fingerprint(preset, vars);

        // Cria job QUEUED
        var job = jobs.createQueued(p.id(), fingerprint);

        try {
            jobs.markRunning(job.id());

            // Prompt determinístico para qualidade consistente
            String prompt = promptFactory.promptForProduto(p);

            var req = new ImageGenRequestDTO(
                    preset,
                    prompt,
                    vars,
                    null,    // inputImageUrl (se tiver uma foto base, preencha aqui)
                    true,    // removeBackground
                    true     // upscale
            );

            // Geração síncrona: só retorna com a URL final pronta
            String resultUrl = imageStudio.generateSync(req);

            // Atualiza produto.imagem
            produtos.updateImagem(p.id(), resultUrl);

            // Finaliza job
            jobs.markDone(job.id(), resultUrl);

            log.info("Imagem gerada com sucesso para produto {} -> {}", p.id(), resultUrl);

        } catch (Exception e) {
            jobs.markError(job.id(), e.getMessage());
            log.error("Falha ao gerar imagem para produto {}: {}", p.id(), e.getMessage(), e);
        }
    }

    /**
     * Regenera imagem mesmo que já exista (modo forçado).
     * Útil para botão de 'Regenerar' no admin.
     */
    @Transactional
    public void regenerateForced(Long productId) {
        var p = produtos.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + productId));

        final String preset = "packshot";
        Map<String, Object> vars = promptFactory.varsFromProduto(p);
        String fingerprint = promptFactory.fingerprint(preset, vars);

        var job = jobs.createQueued(p.id(), fingerprint);
        try {
            jobs.markRunning(job.id());

            var req = new ImageGenRequestDTO(
                    preset,
                    promptFactory.promptForProduto(p),
                    vars,
                    null,
                    true,
                    true
            );

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
