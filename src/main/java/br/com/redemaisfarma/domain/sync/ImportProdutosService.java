// src/main/java/br/com/redemaisfarma/domain/sync/ImportProdutosService.java
package br.com.redemaisfarma.domain.sync;

import br.com.redemaisfarma.adapters.outbound.firebird.FirebirdProdutoDao;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
public class ImportProdutosService {

    private final FirebirdProdutoDao dao;
    private final ProdutoRepository repo;

    public ImportProdutosService(FirebirdProdutoDao dao, ProdutoRepository repo) {
        this.dao = dao;
        this.repo = repo;
    }

    @Transactional
    public LocalDateTime importarLote(LocalDateTime since, int limit) {
        List<FirebirdProdutoDao.FbProduto> lote = dao.fetchAtualizados(since, limit);
        if (lote.isEmpty()) return since;

        LocalDateTime maxTs = since;

        for (var fb : lote) {
            String ean = normalizeEan(fb.codBarras());
            if (ean == null) continue;

            ProdutoEntity p = repo.findByCodigoBarras(ean).orElseGet(ProdutoEntity::new);

            if (p.getId() == null) {
                p.setCodigoBarras(ean);
                p.setDataCadastro(LocalDate.now());
            }

            p.setLegacyId(fb.produtoId() != null ? fb.produtoId().longValue() : null);
            p.setNome(nvl(fb.produto(), "Produto"));
            p.setDescricao(nvl(fb.apresentacao(), null));

            // preços
            p.setPrecoVenda(defaultPositive(fb.precoVenda()));
            p.setPrecoPromocional(normalizePromo(fb.precoPromocao()));
            p.setPrecoCusto(defaultPrecoCusto(null, p.getPrecoVenda())); // legado não envia custo

            // estoque / disponibilidade
            int estoque = fb.estoque() == null ? 0 : Math.max(0, fb.estoque());
            p.setEstoque(estoque);

            boolean disponivel = estoque > 0 && p.getPrecoVenda().compareTo(BigDecimal.ZERO) > 0;
            p.setDisponivel(disponivel);

            // status
            p.setStatusSync("SINCRONIZADO");

            // Mantém PUBLICADO, mas garante o "published_at" por causa do check do banco
            p.setStatus(ProdutoStatus.PUBLICADO);
            if (p.getStatus() == ProdutoStatus.PUBLICADO) {
                if (p.getPublicadoEm() == null) {
                    p.setPublicadoEm(LocalDateTime.now());
                }
                // alguns esquemas exigem despublicado_em NULL quando publicado
                p.setDespublicadoEm(null);
            }

            // desconto_percentual — calcula só quando há base > 0
            if (p.getPrecoPromocional() != null
                    && p.getPrecoPromocional().compareTo(BigDecimal.ZERO) > 0
                    && p.getPrecoVenda() != null
                    && p.getPrecoVenda().compareTo(BigDecimal.ZERO) > 0) {
                double pct = 100.0 * (1.0 - p.getPrecoPromocional().doubleValue() / p.getPrecoVenda().doubleValue());
                p.setDescontoPercentual((int) Math.round(pct));
            } else {
                p.setDescontoPercentual(null);
            }

            // ⚙️ hash_legado (NOT NULL no banco): gera SHA-256 estável a partir de campos do legado
            String fingerprint = buildFingerprint(fb, ean);
            p.setHashLegado(sha256Hex(fingerprint));

            // opcional: validador pode receber o próprio EAN
            if (p.getValidador() == null || p.getValidador().isBlank()) {
                p.setValidador(ean);
            }

            repo.save(p);

            if (fb.lastUpdate() != null && fb.lastUpdate().isAfter(maxTs)) {
                maxTs = fb.lastUpdate();
            }
        }
        return maxTs;
    }

    // ------- helpers -------

    private static String buildFingerprint(FirebirdProdutoDao.FbProduto fb, String ean) {
        return String.join("|",
                nz(ean),
                nz(fb.produtoId()),
                nz(fb.produto()),
                nz(fb.apresentacao()),
                nz(fb.precoVenda()),
                nz(fb.precoPromocao()),
                nz(fb.estoque()),
                nz(fb.lastUpdate())
        );
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            return "sha256_error_" + Integer.toHexString(s.hashCode());
        }
    }

    private static String nz(Object o) { return o == null ? "" : o.toString().trim(); }
    private static String nvl(String v, String def) { return (v == null || v.isBlank()) ? def : v.trim(); }
    private static String normalizeEan(String e) {
        if (e == null) return null;
        String d = e.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }
    private static BigDecimal defaultPositive(BigDecimal v) {
        return (v == null || v.compareTo(BigDecimal.ZERO) < 0) ? BigDecimal.ZERO : v;
    }
    private static BigDecimal normalizePromo(BigDecimal v) {
        if (v == null) return null;
        return (v.compareTo(BigDecimal.ZERO) > 0) ? v : null;
    }
    private static BigDecimal defaultPrecoCusto(BigDecimal custoLido, BigDecimal precoVenda) {
        if (custoLido != null && custoLido.compareTo(BigDecimal.ZERO) >= 0) return custoLido;
        return BigDecimal.ZERO; // NOT NULL no banco
    }
}
