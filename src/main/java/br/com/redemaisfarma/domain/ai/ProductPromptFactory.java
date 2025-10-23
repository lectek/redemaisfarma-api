package br.com.redemaisfarma.domain.ai;

import br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort.ProdutoDTO;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

@Component
public class ProductPromptFactory {

    public Map<String,Object> varsFromProduto(ProdutoDTO p) {
        return Map.of(
            "descricao",  safe(p.descricao()),
            "categoria",  safe(p.categoria()),
            "codigo",     safe(p.codigoBarras()),
            "cores",      List.of("#0077FF", "#00CC88") // ajuste cores da marca
        );
    }

    public String promptForProduto(ProdutoDTO p) {
        return """
               product studio photography, clean white seamless background, soft shadow,
               centered, high detail, commercial e-commerce packshot,
               show only the product, no extra props, no text, no watermark
               """;
    }

    /** SHA-256 sem libs externas. */
    public String fingerprint(String preset, Map<String,Object> vars) {
        String input = preset + "::" + vars.toString();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    private static String toHex(byte[] bytes) {
        try (Formatter f = new Formatter()) {
            for (byte b : bytes) f.format("%02x", b);
            return f.toString();
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
}
