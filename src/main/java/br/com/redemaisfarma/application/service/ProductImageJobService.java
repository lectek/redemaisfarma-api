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
import java.util.Set;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductImageJobService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageJobService.class);
    private static final String PRESET = "packshot";
    private static final Duration HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(12);
    private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(25);
    private static final Duration HTTP_RETRY_BASE_DELAY = Duration.ofMillis(700);
    private static final int HTTP_DOWNLOAD_MAX_ATTEMPTS = 3;
    private static final String HTTP_ERROR_PREFIX = "Falha ao baixar imagem da IA. HTTP ";
    private static final String IMAGE_FETCH_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
    private static final Set<Integer> RETRYABLE_HTTP_STATUS = Set.of(
            408, 425, 429, 500, 502, 503, 504, 520, 521, 522, 523, 524, 525, 526, 527, 529, 530
    );

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
            String persistedPngUrl = persistImageWithFallback(p.id(), generatedUrl);
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
            String persistedPngUrl = persistImageWithFallback(p.id(), generatedUrl);
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

        final URI imageUri;
        try {
            imageUri = URI.create(generatedUrl);
        } catch (IllegalArgumentException ex) {
            throw new IOException("URL gerada da imagem e invalida.", ex);
        }

        final byte[] body = downloadImageBodyWithRetry(imageUri);
        if (body == null || body.length == 0) {
            throw new IOException("Conteudo da imagem gerada esta vazio.");
        }

        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(body));
        if (image == null) {
            throw new IOException("Formato da imagem gerada nao e suportado para conversao.");
        }

        return this.imageStorageService.saveProductImagePng(productId, image);
    }

    private byte[] downloadImageBodyWithRetry(final URI imageUri) throws IOException {
        IOException lastIo = null;
        int lastStatus = -1;

        for (int attempt = 1; attempt <= HTTP_DOWNLOAD_MAX_ATTEMPTS; attempt++) {
            final HttpRequest request = HttpRequest.newBuilder(imageUri)
                    .GET()
                    .timeout(HTTP_REQUEST_TIMEOUT)
                    .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                    .header("User-Agent", IMAGE_FETCH_USER_AGENT)
                    .header("Referer", "https://image.pollinations.ai/")
                    .build();

            final HttpResponse<byte[]> response;
            try {
                response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Download da imagem foi interrompido.", ex);
            } catch (IOException ex) {
                lastIo = ex;
                if (attempt < HTTP_DOWNLOAD_MAX_ATTEMPTS) {
                    sleepBeforeRetry(attempt);
                    continue;
                }
                throw ex;
            }

            final int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            }

            lastStatus = status;
            if (attempt < HTTP_DOWNLOAD_MAX_ATTEMPTS && RETRYABLE_HTTP_STATUS.contains(status)) {
                sleepBeforeRetry(attempt);
                continue;
            }
            break;
        }

        if (lastStatus > 0) {
            throw new IOException("Falha ao baixar imagem da IA. HTTP " + lastStatus);
        }
        throw new IOException("Falha ao baixar imagem da IA.", lastIo);
    }

    private static void sleepBeforeRetry(final int attempt) throws IOException {
        final long delayMs = HTTP_RETRY_BASE_DELAY.toMillis() * attempt;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry de download foi interrompido.", ex);
        }
    }

    private String persistImageWithFallback(final Long productId, final String generatedUrl)
            throws IOException {
        try {
            return persistAsPng(productId, generatedUrl);
        } catch (IOException ex) {
            if (canFallbackToRemoteUrl(generatedUrl, ex)) {
                log.warn(
                        "Fallback para URL remota da IA no produto {}: {}",
                        productId,
                        ex.getMessage()
                );
                return generatedUrl.trim();
            }
            throw ex;
        }
    }

    private static boolean canFallbackToRemoteUrl(final String generatedUrl, final IOException ex) {
        if (generatedUrl == null || generatedUrl.isBlank()) {
            return false;
        }
        final String trimmed = generatedUrl.trim();
        if (!(trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
            return false;
        }
        final String message = ex.getMessage() == null ? "" : ex.getMessage();
        return message.startsWith(HTTP_ERROR_PREFIX)
                || message.contains("Conteudo da imagem gerada esta vazio")
                || message.contains("Formato da imagem gerada nao e suportado");
    }
}
