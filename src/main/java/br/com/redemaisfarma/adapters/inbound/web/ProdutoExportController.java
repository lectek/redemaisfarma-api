/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.ServletOutputStream
 *  jakarta.servlet.http.HttpServletResponse
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PatchMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/admin/produtos"})
public class ProdutoExportController {
    private final ProdutoRepository produtoRepository;

    public ProdutoExportController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping(value={"/export"}, produces={"text/csv; charset=UTF-8"})
    public void exportCsv(@RequestParam(name="format", defaultValue="csv") String format, @RequestParam(name="status", required=false) ProdutoStatus status, HttpServletResponse resp) throws Exception {
        List produtos;
        List list = produtos = status == null ? this.produtoRepository.findAll() : this.produtoRepository.findByStatus(status, (Pageable)PageRequest.of((int)0, (int)2000)).getContent();
        if (!"csv".equalsIgnoreCase(format)) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            String json = ProdutoExportController.toJson(produtos);
            resp.getWriter().write(json);
            return;
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "produtos_" + timestamp + ".csv";
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        ServletOutputStream os = resp.getOutputStream();
        os.write(239);
        os.write(187);
        os.write(191);
        try (OutputStreamWriter writer = new OutputStreamWriter((OutputStream)os, StandardCharsets.UTF_8);){
            writer.write("id;nome;descricao;codigoBarras;categoria;precoVenda;precoCusto;estoque;disponivel;status\r\n");
            for (ProdutoEntity p : produtos) {
                writer.write(String.join((CharSequence)";", ProdutoExportController.s(p.getId()), ProdutoExportController.s(p.getNome()), ProdutoExportController.s(p.getDescricao()), ProdutoExportController.s(p.getCodigoBarras()), ProdutoExportController.s(p.getCategoria()), ProdutoExportController.s(p.getPrecoVenda()), ProdutoExportController.s(p.getPrecoCusto()), ProdutoExportController.s(p.getEstoque()), ProdutoExportController.s(p.getDisponivel()), ProdutoExportController.s((Object)p.getStatus())) + "\r\n");
            }
        }
    }

    @GetMapping(value={"/pending"})
    public List<ProdutoEntity> listPending() {
        return this.produtoRepository.findByStatusOrderByDataImportacaoAsc(ProdutoStatus.IMPORTADO, (Pageable)PageRequest.of((int)0, (int)100)).getContent();
    }

    @PatchMapping(value={"/{id}/status"})
    public ProdutoEntity updateStatus(@PathVariable Long id, @RequestParam ProdutoStatus status, @RequestParam(required=false) String validador) {
        ProdutoEntity produto = (ProdutoEntity)this.produtoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto n\u00e3o encontrado: " + id));
        produto.setStatus(status);
        produto.setValidador(validador);
        if (status == ProdutoStatus.PUBLICADO) {
            produto.setPublicadoEm(LocalDateTime.now());
        }
        return (ProdutoEntity)this.produtoRepository.save(produto);
    }

    private static String s(Object v) {
        if (v == null) {
            return "\"\"";
        }
        String raw = String.valueOf(v);
        String esc = raw.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ");
        return "\"" + esc + "\"";
    }

    private static String toJson(List<ProdutoEntity> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); ++i) {
            ProdutoEntity p = list.get(i);
            sb.append("{").append(ProdutoExportController.j("id", p.getId())).append(',').append(ProdutoExportController.j("nome", p.getNome())).append(',').append(ProdutoExportController.j("descricao", p.getDescricao())).append(',').append(ProdutoExportController.j("codigoBarras", p.getCodigoBarras())).append(',').append(ProdutoExportController.j("categoria", p.getCategoria())).append(',').append(ProdutoExportController.j("status", (Object)p.getStatus())).append("}");
            if (i >= list.size() - 1) continue;
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String j(String k, Object v) {
        if (v == null) {
            return "\"" + k + "\":null";
        }
        if (v instanceof Number || v instanceof Boolean) {
            return "\"" + k + "\":" + String.valueOf(v);
        }
        String esc = String.valueOf(v).replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + k + "\":\"" + esc + "\"";
    }
}

