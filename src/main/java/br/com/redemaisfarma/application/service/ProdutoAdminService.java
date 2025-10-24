/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProdutoAdminService {
    private final ProdutoRepository repository;

    public Page<ProdutoEntity> buscarPagina(String q, String categoria, Pageable pageable) {
        String qNorm = ProdutoAdminService.normalize(q);
        String catNorm = ProdutoAdminService.normalize(categoria);
        return this.repository.searchPageByCategoria(qNorm, catNorm, pageable);
    }

    public List<ProdutoEntity> buscarParaExport(String q, String categoria, int limit) {
        String qNorm = ProdutoAdminService.normalize(q);
        String catNorm = ProdutoAdminService.normalize(categoria);
        int safeLimit = Math.max(1, Math.min(limit, 50000));
        if (qNorm == null && catNorm == null) {
            List<ProdutoEntity> all = this.repository.findTop2000ByOrderByIdAsc();
            return safeLimit < 2000 ? all.stream().limit(safeLimit).toList() : all;
        }
        return this.repository.searchForExportLimited(qNorm, catNorm, Math.min(safeLimit, 2000));
    }

    public void writeCsv(OutputStream os, String q, String categoria, int limit) throws IOException {
        List<ProdutoEntity> produtos = this.buscarParaExport(q, categoria, limit);
        Throwable throwable = null;
        Object var7_8 = null;
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));){
            w.write(65279);
            w.write("id;descricao;codigo_barras;preco_venda\n");
            for (ProdutoEntity p : produtos) {
                w.write(ProdutoAdminService.csv(p.getId()));
                w.write(59);
                w.write(ProdutoAdminService.csv(p.getDescricao()));
                w.write(59);
                w.write(ProdutoAdminService.csv(p.getCodigoBarras()));
                w.write(59);
                w.write(ProdutoAdminService.csv(p.getPrecoVenda()));
                w.write(10);
            }
            w.flush();
        }
        catch (Throwable throwable2) {
            if (throwable == null) {
                throwable = throwable2;
            } else if (throwable != throwable2) {
                throwable.addSuppressed(throwable2);
            }
            throw throwable;
        }
    }

    private static String csv(Object o) {
        if (o == null) {
            return "";
        }
        String s = String.valueOf(o);
        boolean wrap = s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        s = s.replace("\"", "\"\"");
        return wrap ? "\"" + s + "\"" : s;
    }

    private static String normalize(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Generated
    public ProdutoAdminService(ProdutoRepository repository) {
        this.repository = repository;
    }
}

