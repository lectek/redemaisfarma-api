/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import lombok.Generated;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix="app.sync.legacy", name={"enabled"}, havingValue="true")
public class ProdutoLegacyExportService {
    private final ProdutoLegacyRepository repository;

    @Transactional(readOnly=true)
    public void writeCsv(OutputStream out) throws IOException {
        out.write(new byte[]{-17, -69, -65});
        Throwable throwable = null;
        Object var3_4 = null;
        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));){
            w.write(String.join((CharSequence)";", "ID", "NOME", "CODIGOBARRAS", "SALDO", "PRECOVENDA", "PRECOPROMOCAO", "ESTOQUEMINIMO", "MARGEMLUCRO", "INICIOPROMOCAO", "TERMINOPROMOCAO", "BONUS", "APRESENTACAO", "PRECOANTERIOR", "FORNECEDOR_ID", "CATEGORIA_ID", "COMISSAO_ID"));
            w.write("\n");
            Throwable throwable2 = null;
            Object var6_9 = null;
            try (Stream<ProdutoLegacyEntity> stream = this.repository.streamAll();){
                stream.forEach(p -> {
                    try {
                        w.write(String.join((CharSequence)";", ProdutoLegacyExportService.csv(p.getId()), ProdutoLegacyExportService.csv(p.getNome()), ProdutoLegacyExportService.csv(p.getCodigoBarras()), ProdutoLegacyExportService.csv(p.getSaldo()), ProdutoLegacyExportService.csv(p.getPrecoVenda()), ProdutoLegacyExportService.csv(p.getPrecoPromocao()), ProdutoLegacyExportService.csv(p.getEstoqueMinimo()), ProdutoLegacyExportService.csv(p.getMargemLucro()), ProdutoLegacyExportService.csv(p.getInicioPromocao()), ProdutoLegacyExportService.csv(p.getTerminoPromocao()), ProdutoLegacyExportService.csv(p.getBonus()), ProdutoLegacyExportService.csv(p.getApresentacao()), ProdutoLegacyExportService.csv(p.getPrecoAnterior()), ProdutoLegacyExportService.csv(p.getFornecedorId()), ProdutoLegacyExportService.csv(p.getCategoriaId()), ProdutoLegacyExportService.csv(p.getComissaoId())));
                        w.write("\n");
                    }
                    catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
            catch (Throwable throwable3) {
                if (throwable2 == null) {
                    throwable2 = throwable3;
                } else if (throwable2 != throwable3) {
                    throwable2.addSuppressed(throwable3);
                }
                throw throwable2;
            }
        }
        catch (Throwable throwable4) {
            if (throwable == null) {
                throwable = throwable4;
            } else if (throwable != throwable4) {
                throwable.addSuppressed(throwable4);
            }
            throw throwable;
        }
    }

    private static String csv(Object v) {
        if (v == null) {
            return "";
        }
        String s = String.valueOf(v);
        s = s.replace("\r", " ").replace("\n", " ");
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    @Generated
    public ProdutoLegacyExportService(ProdutoLegacyRepository repository) {
        this.repository = repository;
    }
}

