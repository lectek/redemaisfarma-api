/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.jdbc.BadSqlGrammarException
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.jdbc.core.RowMapper
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.outbound.legacy.adapter;

import br.com.redemaisfarma.adapters.outbound.legacy.dto.LegacyProdutoDTO;
import br.com.redemaisfarma.adapters.outbound.legacy.port.LegacyProdutoPort;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LegacyProdutoJdbcAdapter
implements LegacyProdutoPort {
    private final JdbcTemplate jdbc;
    private final String configuredLastUpdateColumn;

    public LegacyProdutoJdbcAdapter(@Qualifier(value="jdbcTemplateFirebird") JdbcTemplate jdbcTemplateFirebird, @Value(value="${legacy.produtos.last-update-column:}") String configuredLastUpdateColumn) {
        this.jdbc = jdbcTemplateFirebird;
        this.configuredLastUpdateColumn = configuredLastUpdateColumn;
    }

    @Override
    public List<LegacyProdutoDTO> fetchChangedSince(LocalDateTime since, int page, int size) {
        String lastCol = this.resolveLastUpdateColumn();
        int offset = Math.max(0, page) * Math.max(1, size);
        String sql = "SELECT FIRST %d SKIP %d\n       ID_PRODUTO,\n       PRODUTO,\n       APRESENTACAO,\n       COD_BARRAS,\n       PROD_PRVENDA,\n       PROD_PRPROMOCAO,\n       PROD_SALDO,\n       %s AS LAST_UPDATE\nFROM PRODUTOS\nWHERE %s >= ?\nORDER BY %s\n".formatted(size, offset, lastCol, lastCol, lastCol);
        return this.jdbc.query(sql, ps -> ps.setTimestamp(1, Timestamp.valueOf(since)), (RowMapper)new LegacyProdutoMapper());
    }

    private String resolveLastUpdateColumn() {
        List<String> candidates = this.configuredLastUpdateColumn != null && !this.configuredLastUpdateColumn.isBlank() ? List.of(this.configuredLastUpdateColumn) : List.of("ULTIMA_ATUALIZACAO", "DT_ALTERACAO", "ULT_ATUALIZACAO", "UPDATED_AT");
        for (String col : candidates) {
            try {
                this.jdbc.query("SELECT " + col + " FROM PRODUTOS WHERE 1=0", rs -> {});
                return col;
            }
            catch (BadSqlGrammarException badSqlGrammarException) {
                // empty catch block
            }
        }
        throw new IllegalStateException("N\u00e3o encontrei coluna de '\u00faltima atualiza\u00e7\u00e3o' na tabela PRODUTOS. Configure-a em legacy.produtos.last-update-column");
    }

    private static class LegacyProdutoMapper
    implements RowMapper<LegacyProdutoDTO> {
        private LegacyProdutoMapper() {
        }

        public LegacyProdutoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            LocalDateTime lastUpdate = null;
            Timestamp ts = rs.getTimestamp("LAST_UPDATE");
            if (ts != null) {
                lastUpdate = ts.toLocalDateTime();
            }
            return new LegacyProdutoDTO(rs.getLong("ID_PRODUTO"), rs.getString("PRODUTO"), rs.getString("APRESENTACAO"), rs.getString("COD_BARRAS"), rs.getBigDecimal("PROD_PRVENDA"), rs.getBigDecimal("PROD_PRPROMOCAO"), rs.getInt("PROD_SALDO"), lastUpdate);
        }
    }
}

