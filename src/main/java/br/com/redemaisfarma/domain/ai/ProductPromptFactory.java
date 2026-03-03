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
        return Map.of(
                "nome", this.safe(p.nome()),
                "descricao", this.safe(p.descricao()),
                "categoria", this.safe(p.categoria()),
                "fabricante", this.safe(p.fabricante()),
                "codigo", this.safe(p.codigoBarras()),
                "cores", List.of("#0077FF", "#00CC88")
        );
    }

    public String promptForProduto(ProdutoRepositoryPort.ProdutoDTO p) {
        StringBuilder sb = new StringBuilder(
                "Packshot profissional de produto farmaceutico para ecommerce, fundo branco puro, "
                        + "luz de estudio suave e sombra discreta.");
        this.appendField(sb, "Produto", p.nome());
        this.appendField(sb, "Descricao", p.descricao());
        this.appendField(sb, "Categoria", p.categoria());
        this.appendField(sb, "Fabricante", p.fabricante());
        this.appendField(sb, "Codigo", p.codigoBarras());
        sb.append(" Mostrar somente o produto, sem textos, sem logo adicional e sem marca d'agua.");
        return sb.toString();
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

    private void appendField(StringBuilder sb, String label, String value) {
        String safeValue = this.safe(value);
        if (!safeValue.isBlank()) {
            sb.append(' ').append(label).append(": ").append(safeValue).append('.');
        }
    }
}
