package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ProdutoAdminService {

    private final ProdutoRepository repository;

    public Page<ProdutoEntity> buscarPagina(String q, String categoria, Pageable pageable) {
        String qNorm = normalize(q);
        String catNorm = normalize(categoria);
        return repository.searchPageByCategoria(qNorm, catNorm, pageable);
    }

    public List<ProdutoEntity> buscarParaExport(String q, String categoria, int limit) {
        String qNorm = normalize(q);
        String catNorm = normalize(categoria);
        int safeLimit = Math.max(1, Math.min(limit, 50_000));

        if (qNorm == null && catNorm == null) {
            List<ProdutoEntity> all = repository.findTop2000ByOrderByIdAsc();
            return safeLimit < 2000 ? all.stream().limit(safeLimit).toList() : all;
        }
        // mesmo com limite alto, o export controlado vai até 2000
        return repository.searchForExportLimited(qNorm, catNorm, Math.min(safeLimit, 2000));
    }

    /** Escreve CSV em UTF-8 (com BOM) e separador ';' */
    public void writeCsv(OutputStream os, String q, String categoria, int limit) throws IOException {
        List<ProdutoEntity> produtos = buscarParaExport(q, categoria, limit);

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            // BOM UTF-8
            w.write('\uFEFF');

            // cabeçalho
            w.append("id;descricao;codigo_barras;preco_venda\n");

            // linhas
            for (ProdutoEntity p : produtos) {
                w.append(csv(p.getId())).append(';')
                 .append(csv(p.getDescricao())).append(';')
                 .append(csv(p.getCodigoBarras())).append(';')
                 .append(csv(formatPreco(p.getPrecoVenda()))).append('\n');
            }
            // try-with-resources já garante flush/close; flush aqui é opcional
            w.flush();
        }
    }

    // -------- helpers --------

    private static String csv(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        boolean wrap = s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        s = s.replace("\"", "\"\""); // escapa aspas
        return wrap ? "\"" + s + "\"" : s;
    }

    private static String formatPreco(BigDecimal v) {
        if (v == null) return "";
        // evita notação científica e vírgula de locale
        return v.stripTrailingZeros().toPlainString();
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Generated
    public ProdutoAdminService(ProdutoRepository repository) {
        this.repository = repository;
    }
}
