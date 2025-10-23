// src/main/java/br/com/redemaisfarma/application/service/ProdutoAdminService.java
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoAdminService {

    private final ProdutoRepository repository;

    /** Lista paginada com busca livre (q) + filtro opcional por categoria. */
    public Page<ProdutoEntity> buscarPagina(String q, String categoria, Pageable pageable) {
        String qNorm = normalize(q);
        String catNorm = normalize(categoria);
        return repository.searchPageByCategoria(qNorm, catNorm, pageable);
    }

    /** Lista limitada para export (respeita busca livre e categoria). */
    public List<ProdutoEntity> buscarParaExport(String q, String categoria, int limit) {
        String qNorm = normalize(q);
        String catNorm = normalize(categoria);
        int safeLimit = Math.max(1, Math.min(limit, 50_000));

        if (qNorm == null && catNorm == null) {
            var all = repository.findTop2000ByOrderByIdAsc();
            return safeLimit < 2000 ? all.stream().limit(safeLimit).toList() : all;
        }
        return repository.searchForExportLimited(qNorm, catNorm, Math.min(safeLimit, 2000));
    }

    /** Escreve CSV com separador ';' + BOM para Excel PT-BR. */
    public void writeCsv(OutputStream os, String q, String categoria, int limit) throws IOException {
        var produtos = buscarParaExport(q, categoria, limit);
        try (var w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            w.write('\uFEFF'); // BOM
            w.write("id;descricao;codigo_barras;preco_venda\n");
            for (var p : produtos) {
                w.write(csv(p.getId())); w.write(';');
                w.write(csv(p.getDescricao())); w.write(';');
                w.write(csv(p.getCodigoBarras())); w.write(';');
                w.write(csv(p.getPrecoVenda())); w.write('\n');
            }
            w.flush();
        }
    }

    // Helpers
    private static String csv(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        boolean wrap = s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        s = s.replace("\"", "\"\"");
        return wrap ? "\"" + s + "\"" : s;
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
