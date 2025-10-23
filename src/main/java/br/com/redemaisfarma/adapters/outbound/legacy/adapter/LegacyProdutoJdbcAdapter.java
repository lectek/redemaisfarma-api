// src/main/java/br/com/redemaisfarma/adapters/outbound/legacy/adapter/LegacyProdutoJdbcAdapter.java
package br.com.redemaisfarma.adapters.outbound.legacy.adapter;

import br.com.redemaisfarma.adapters.outbound.legacy.dto.LegacyProdutoDTO;
import br.com.redemaisfarma.adapters.outbound.legacy.port.LegacyProdutoPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementação real do LegacyProdutoPort usando Firebird via JDBC.
 */
@Component
public class LegacyProdutoJdbcAdapter implements LegacyProdutoPort {

    private final JdbcTemplate jdbc;

    /**
     * Nome configurável da coluna de "última atualização" no Firebird.
     * Ex.: DT_ALTERACAO, ULT_ATUALIZACAO, UPDATED_AT, etc.
     */
    private final String configuredLastUpdateColumn;

    // O bean passado aqui deve ser o JdbcTemplate configurado para o Firebird
    public LegacyProdutoJdbcAdapter(
            @Qualifier("jdbcTemplateFirebird") JdbcTemplate jdbcTemplateFirebird,
            @Value("${legacy.produtos.last-update-column:}") String configuredLastUpdateColumn
    ) {
        this.jdbc = jdbcTemplateFirebird;
        this.configuredLastUpdateColumn = configuredLastUpdateColumn;
    }

    @Override
    public List<LegacyProdutoDTO> fetchChangedSince(LocalDateTime since, int page, int size) {
        String lastCol = resolveLastUpdateColumn();
        int offset = Math.max(0, page) * Math.max(1, size);

        // Evita parâmetros em FIRST/SKIP (alguns drivers/versões do FB não aceitam)
        String sql = """
            SELECT FIRST %d SKIP %d
                   ID_PRODUTO,
                   PRODUTO,
                   APRESENTACAO,
                   COD_BARRAS,
                   PROD_PRVENDA,
                   PROD_PRPROMOCAO,
                   PROD_SALDO,
                   %s AS LAST_UPDATE
            FROM PRODUTOS
            WHERE %s >= ?
            ORDER BY %s
            """.formatted(size, offset, lastCol, lastCol, lastCol);

        return jdbc.query(
                sql,
                ps -> ps.setTimestamp(1, java.sql.Timestamp.valueOf(since)),
                new LegacyProdutoMapper()
        );
    }

    /**
     * Descobre a coluna de "última atualização" testando a configurada e alguns candidatos comuns.
     */
    private String resolveLastUpdateColumn() {
        List<String> candidates = (configuredLastUpdateColumn != null && !configuredLastUpdateColumn.isBlank())
                ? List.of(configuredLastUpdateColumn)
                : List.of("ULTIMA_ATUALIZACAO", "DT_ALTERACAO", "ULT_ATUALIZACAO", "UPDATED_AT");

        for (String col : candidates) {
            try {
                // Testa sintaxe/coluna sem retornar dados
                jdbc.query("SELECT " + col + " FROM PRODUTOS WHERE 1=0", rs -> {});
                return col;
            } catch (BadSqlGrammarException ignore) {
                // tenta o próximo
            }
        }
        throw new IllegalStateException(
                "Não encontrei coluna de 'última atualização' na tabela PRODUTOS. " +
                "Configure-a em legacy.produtos.last-update-column"
        );
    }

    private static class LegacyProdutoMapper implements RowMapper<LegacyProdutoDTO> {
        @Override
        public LegacyProdutoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            LocalDateTime lastUpdate = null;
            var ts = rs.getTimestamp("LAST_UPDATE");
            if (ts != null) lastUpdate = ts.toLocalDateTime();

            return new LegacyProdutoDTO(
                    rs.getLong("ID_PRODUTO"),
                    rs.getString("PRODUTO"),
                    rs.getString("APRESENTACAO"),
                    rs.getString("COD_BARRAS"),
                    rs.getBigDecimal("PROD_PRVENDA"),
                    rs.getBigDecimal("PROD_PRPROMOCAO"),
                    rs.getInt("PROD_SALDO"),
                    lastUpdate
            );
        }
    }
}
