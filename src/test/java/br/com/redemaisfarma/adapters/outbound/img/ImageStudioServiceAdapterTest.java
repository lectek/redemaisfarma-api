package br.com.redemaisfarma.adapters.outbound.img;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ImageStudioServiceAdapterTest {

    @Test
    void shouldCapGeneratedPollinationsUrlToConfiguredLimit() {
        ImageStudioServiceAdapter adapter = new ImageStudioServiceAdapter(
                "pollinations",
                "https://image.pollinations.ai/prompt",
                "flux",
                1024,
                1024,
                true,
                false,
                240
        );

        String veryLongDescription = "Dipirona sodica 500mg com acao analgesica e antitermica para alivio da dor e febre "
                + "uso adulto e pediatrico sob orientacao profissional em embalagem promocional com detalhes extensos "
                + "sobre composicao, modo de usar, restricoes e informacoes de bula repetidas para simular cadastro legado";

        ImageGenRequestDTO request = new ImageGenRequestDTO(
                "packshot",
                null,
                Map.of(
                        "descricao", veryLongDescription,
                        "categoria", "Medicacoes",
                        "codigo", "7896000000001"
                ),
                null,
                true,
                true
        );

        String url = adapter.generateSync(request);

        assertThat(url).startsWith("https://image.pollinations.ai/prompt/");
        assertThat(url).contains("model=flux");
        assertThat(url.length()).isLessThanOrEqualTo(240);
    }

    @Test
    void shouldSanitizePathSeparatorsInPromptFields() {
        ImageStudioServiceAdapter adapter = new ImageStudioServiceAdapter(
                "pollinations",
                "https://image.pollinations.ai/prompt",
                "flux",
                1024,
                1024,
                true,
                false,
                512
        );

        ImageGenRequestDTO request = new ImageGenRequestDTO(
                "packshot",
                null,
                Map.of(
                        "descricao", "COLETOR CRISTAL TAMPA CRISTALC/PA",
                        "categoria", "MEDICACOES",
                        "codigo", "7896000000001"
                ),
                null,
                true,
                true
        );

        String url = adapter.generateSync(request);

        assertThat(url).startsWith("https://image.pollinations.ai/prompt/");
        assertThat(url).doesNotContain("%2F");
        assertThat(url).contains("CRISTALC%20PA");
    }
}
