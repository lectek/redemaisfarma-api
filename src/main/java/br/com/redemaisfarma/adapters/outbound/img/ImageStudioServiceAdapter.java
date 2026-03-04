package br.com.redemaisfarma.adapters.outbound.img;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class ImageStudioServiceAdapter implements ImageStudioUseCase {

    private static final String PROVIDER_POLLINATIONS = "pollinations";
    private static final String PROVIDER_STUB = "stub";
    private static final String DEFAULT_STUB_BASE_URL = "http://localhost:8080/assets/autogen";
    private static final int DEFAULT_MIN_PROMPT_CHARS = 32;
    private static final int DEFAULT_MAX_PROMPT_FIELD_CHARS = 140;

    private final String provider;
    private final String pollinationsBaseUrl;
    private final String pollinationsModel;
    private final int pollinationsWidth;
    private final int pollinationsHeight;
    private final boolean pollinationsNoLogo;
    private final boolean pollinationsPrivate;
    private final int pollinationsMaxUrlLength;

    public ImageStudioServiceAdapter(
            @Value("${app.ai.image.provider:pollinations}") String provider,
            @Value("${app.ai.image.pollinations.base-url:https://image.pollinations.ai/prompt}") String pollinationsBaseUrl,
            @Value("${app.ai.image.pollinations.model:flux}") String pollinationsModel,
            @Value("${app.ai.image.pollinations.width:1024}") int pollinationsWidth,
            @Value("${app.ai.image.pollinations.height:1024}") int pollinationsHeight,
            @Value("${app.ai.image.pollinations.nologo:true}") boolean pollinationsNoLogo,
            @Value("${app.ai.image.pollinations.private:false}") boolean pollinationsPrivate,
            @Value("${app.ai.image.pollinations.max-url-length:240}") int pollinationsMaxUrlLength
    ) {
        this.provider = provider == null ? PROVIDER_POLLINATIONS : provider.trim().toLowerCase();
        this.pollinationsBaseUrl = blankToDefault(pollinationsBaseUrl, "https://image.pollinations.ai/prompt");
        this.pollinationsModel = blankToDefault(pollinationsModel, "flux");
        this.pollinationsWidth = Math.clamp(pollinationsWidth, 256, 2048);
        this.pollinationsHeight = Math.clamp(pollinationsHeight, 256, 2048);
        this.pollinationsNoLogo = pollinationsNoLogo;
        this.pollinationsPrivate = pollinationsPrivate;
        this.pollinationsMaxUrlLength = Math.clamp(pollinationsMaxUrlLength, 160, 2048);
    }

    public String generateSync(ImageGenRequestDTO req) {
        if (PROVIDER_STUB.equals(this.provider)) {
            return this.stubUrl(req);
        }
        return this.pollinationsUrl(req);
    }

    private String stubUrl(ImageGenRequestDTO req) {
        String key = req.vars() != null ? String.valueOf(req.vars().getOrDefault("codigo", "produto")) : "produto";
        return DEFAULT_STUB_BASE_URL + "/" + req.preset() + "-" + key + ".png";
    }

    private String pollinationsUrl(ImageGenRequestDTO req) {
        String prompt = buildPrompt(req);
        String url = buildPollinationsUrl(prompt);
        if (url.length() <= this.pollinationsMaxUrlLength) {
            return url;
        }

        String compactPrompt = buildCompactPrompt(req);
        return shrinkToFit(compactPrompt);
    }

    private String buildPollinationsUrl(String prompt) {
        String encodedPrompt = urlEncode(prompt);
        String model = urlEncode(this.pollinationsModel);
        StringBuilder url = new StringBuilder(this.pollinationsBaseUrl.replaceAll("/+$", ""));
        url.append('/').append(encodedPrompt)
                .append("?model=").append(model)
                .append("&width=").append(this.pollinationsWidth)
                .append("&height=").append(this.pollinationsHeight);
        if (this.pollinationsNoLogo) {
            url.append("&nologo=true");
        }
        if (this.pollinationsPrivate) {
            url.append("&private=true");
        }
        return url.toString();
    }

    private String shrinkToFit(String prompt) {
        String workingPrompt = normalizePrompt(prompt);
        String url = buildPollinationsUrl(workingPrompt);

        while (url.length() > this.pollinationsMaxUrlLength && workingPrompt.length() > DEFAULT_MIN_PROMPT_CHARS) {
            int nextLength = Math.max(DEFAULT_MIN_PROMPT_CHARS, workingPrompt.length() - 12);
            workingPrompt = workingPrompt.substring(0, nextLength).trim();

            int lastSpace = workingPrompt.lastIndexOf(' ');
            if (lastSpace >= DEFAULT_MIN_PROMPT_CHARS) {
                workingPrompt = workingPrompt.substring(0, lastSpace).trim();
            }
            url = buildPollinationsUrl(workingPrompt);
        }

        return url.toString();
    }

    private static String buildPrompt(ImageGenRequestDTO req) {
        String basePrompt = blankToDefault(
                req.prompt(),
                "Packshot de produto farmaceutico para ecommerce em fundo branco, luz de estudio e sombra suave.");

        StringBuilder sb = new StringBuilder(normalizePrompt(basePrompt));
        appendField(sb, "Produto", clip(req.varText("nome", ""), 64));
        appendField(sb, "Descricao", clip(req.varText("descricao", ""), DEFAULT_MAX_PROMPT_FIELD_CHARS));
        appendField(sb, "Categoria", clip(req.varText("categoria", ""), 48));
        appendField(sb, "Fabricante", clip(req.varText("fabricante", ""), 48));
        appendField(sb, "Codigo", clip(req.varText("codigo", ""), 32));

        sb.append(" Mostrar somente o produto, sem pessoas, sem textos extras e sem marca dagua.");
        return normalizePrompt(sb.toString());
    }

    private static String buildCompactPrompt(ImageGenRequestDTO req) {
        StringBuilder sb = new StringBuilder(
                "Packshot de produto farmaceutico para ecommerce, fundo branco puro.");
        appendField(sb, "Descricao", clip(req.varText("descricao", ""), 60));
        appendField(sb, "Categoria", clip(req.varText("categoria", ""), 32));
        appendField(sb, "Codigo", clip(req.varText("codigo", ""), 20));
        sb.append(" Sem pessoas e sem textos.");
        return normalizePrompt(sb.toString());
    }

    private static void appendField(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        sb.append(' ').append(label).append(": ").append(value.trim()).append('.');
    }

    private static String blankToDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static String clip(String value, int maxChars) {
        if (value == null) {
            return "";
        }
        String normalized = normalizePrompt(value);
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxChars)).trim();
    }

    private static String normalizePrompt(String value) {
        if (value == null) {
            return "";
        }
        // Pollinations recebe o prompt na path; separadores podem quebrar o roteamento.
        String sanitized = value
                .replace('/', ' ')
                .replace('\\', ' ')
                .replace('|', ' ');
        return sanitized.replaceAll("\\s+", " ").trim();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
