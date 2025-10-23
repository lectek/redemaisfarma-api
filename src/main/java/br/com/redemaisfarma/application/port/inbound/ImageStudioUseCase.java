package br.com.redemaisfarma.application.port.inbound;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;

/** Porta para o pipeline de geração/edição de imagens. */
public interface ImageStudioUseCase {
    /**
     * Executa o pipeline e retorna a URL pública final (ex.: CDN ou /assets/...).
     * A implementação pode ser sync ou fazer polling interno de um job async.
     */
    String generateSync(ImageGenRequestDTO req);
}
