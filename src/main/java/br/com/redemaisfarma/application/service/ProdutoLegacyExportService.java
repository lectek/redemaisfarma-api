// src/main/java/br/com/redemaisfarma/application/service/ProdutoLegacyExportService.java
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.sync.legacy", name = "enabled", havingValue = "true")
public class ProdutoLegacyExportService {

    private final ProdutoLegacyRepository repository;

    @Transactional(readOnly = true)
    public void writeCsv(OutputStream out) throws IOException {
        // BOM para Excel reconhecer UTF-8
        out.write(new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF});

        try (Writer w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            // cabeçalho
            w.write(String.join(";", new String[]{
                "ID","NOME","CODIGOBARRAS","SALDO","PRECOVENDA","PRECOPROMOCAO",
                "ESTOQUEMINIMO","MARGEMLUCRO","INICIOPROMOCAO","TERMINOPROMOCAO",
                "BONUS","APRESENTACAO","PRECOANTERIOR","FORNECEDOR_ID","CATEGORIA_ID","COMISSAO_ID"
            }));
            w.write("\n");

            try (Stream<ProdutoLegacyEntity> stream = repository.streamAll()) {
                stream.forEach(p -> {
                    try {
                        w.write(String.join(";", new String[]{
                            csv(p.getId()),
                            csv(p.getNome()),
                            csv(p.getCodigoBarras()),
                            csv(p.getSaldo()),
                            csv(p.getPrecoVenda()),
                            csv(p.getPrecoPromocao()),
                            csv(p.getEstoqueMinimo()),
                            csv(p.getMargemLucro()),
                            csv(p.getInicioPromocao()),
                            csv(p.getTerminoPromocao()),
                            csv(p.getBonus()),
                            csv(p.getApresentacao()),
                            csv(p.getPrecoAnterior()),
                            csv(p.getFornecedorId()),
                            csv(p.getCategoriaId()),
                            csv(p.getComissaoId())
                        }));
                        w.write("\n");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            }
        }
    }

    private static String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        s = s.replace("\r", " ").replace("\n", " "); // remove quebras de linha
        s = s.replace("\"", "\"\"");                 // escapa aspas
        return "\"" + s + "\"";                      // sempre entre aspas
    }
}
