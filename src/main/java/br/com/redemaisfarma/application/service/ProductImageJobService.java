package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort;
import br.com.redemaisfarma.domain.ai.ProductPromptFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductImageJobService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageJobService.class);
    private static final String PRESET = "packshot";
    private static final Duration HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(12);
    private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(25);

    private final ProdutoRepositoryPort produtos;
    private final ProductImageJobRepository jobs;
    private final ImageStudioUseCase imageStudio;
    private final ProductPromptFactory promptFactory;
    private final ImageStorageService imageStorageService;
    private final HttpClient httpClient;

    public ProductImageJobService(ProdutoRepositoryPort produtos,
                                  ProductImageJobRepository jobs,
                                  ImageStudioUseCase imageStudio,
                                  ProductPromptFactory promptFactory,
                                  ImageStorageService imageStorageService) {
        this.produtos = produtos;
        this.jobs = jobs;
        this.imageStudio = imageStudio;
        this.promptFactory = promptFactory;
        this.imageStorageService = imageStorageService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
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

            String generatedUrl = imageStudio.generateSync(req);
            String persistedPngUrl = persistAsPng(p.id(), generatedUrl);
            produtos.updateImagem(p.id(), persistedPngUrl);
            jobs.markDone(job.id(), persistedPngUrl);

            log.info("Imagem gerada com sucesso para produto {} -> {}", p.id(), persistedPngUrl);
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

            String generatedUrl = imageStudio.generateSync(req);
            String persistedPngUrl = persistAsPng(p.id(), generatedUrl);
            produtos.updateImagem(p.id(), persistedPngUrl);
            jobs.markDone(job.id(), persistedPngUrl);

            log.info("[FORCED] Imagem regenerada para produto {} -> {}", p.id(), persistedPngUrl);
        } catch (Exception e) {
            jobs.markError(job.id(), e.getMessage());
            log.error("[FORCED] Falha ao regenerar imagem para produto {}: {}", p.id(), e.getMessage(), e);
        }
    }

    private String persistAsPng(final Long productId, final String generatedUrl)
            throws IOException {
        if (generatedUrl == null || generatedUrl.isBlank()) {
            throw new IOException("URL gerada da imagem esta vazia.");
        }

        final HttpRequest request;
        try {
            request = HttpRequest.newBuilder(URI.create(generatedUrl))
                    .GET()
                    .timeout(HTTP_REQUEST_TIMEOUT)
                    .header("Accept", "image/*")
                    .header("User-Agent", "RedeMaisFarma/1.0")
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new IOException("URL gerada da imagem e invalida.", ex);
        }

        final HttpResponse<byte[]> response;
        try {
            response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Download da imagem foi interrompido.", ex);
        }

        final int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new IOException("Falha ao baixar imagem da IA. HTTP " + status);
        }

        final byte[] body = response.body();
        if (body == null || body.length == 0) {
            throw new IOException("Conteudo da imagem gerada esta vazio.");
        }

        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(body));
        if (image == null) {
            throw new IOException("Formato da imagem gerada nao e suportado para conversao.");
        }

        return this.imageStorageService.saveProductImagePng(productId, image);
    }
}
