/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.domain.ai;

import br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProductPromptFactory {
    public Map<String, Object> varsFromProduto(ProdutoRepositoryPort.ProdutoDTO p) {
        return Map.of("descricao", this.safe(p.descricao()), "categoria", this.safe(p.categoria()), "codigo", this.safe(p.codigoBarras()), "cores", List.of("#0077FF", "#00CC88"));
    }

    public String promptForProduto(ProdutoRepositoryPort.ProdutoDTO p) {
        return "product studio photography, clean white seamless background, soft shadow,\ncentered, high detail, commercial e-commerce packshot,\nshow only the product, no extra props, no text, no watermark\n";
    }

    public String fingerprint(String preset, Map<String, Object> vars) {
        String input = preset + "::" + vars.toString();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return ProductPromptFactory.toHex(digest);
        }
        catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    private static String toHex(byte[] bytes) {
        try (Formatter f = new Formatter();){
            for (byte b : bytes) {
                f.format("%02x", b);
            }
            String string = f.toString();
            return string;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

