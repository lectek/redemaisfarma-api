package br.com.redemaisfarma.adapters.outbound.firebird;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class FirebirdProdutoDao {

    private final JdbcTemplate fb;

    public record FbProduto(
        Integer produtoId,
        String produto,
        String apresentacao,
        String codBarras,
        BigDecimal precoVenda,
        BigDecimal precoPromocao,
        Integer estoque,
        LocalDateTime lastUpdate
    ) {}

    private static final RowMapper<FbProduto> MAPPER = (ResultSet rs, int i) -> new FbProduto(
        rs.getInt("PRODUTO_ID"),
        rs.getString("PRODUTO"),
        rs.getString("APRESENTACAO"),
        rs.getString("COD_BARRAS"),
        rs.getBigDecimal("PROD_PRVENDA"),
        rs.getBigDecimal("PROD_PRPROMOCAO"),
        safeInt(rs, "PROD_SALDO"),
        safeTs(rs, "LASTUPDATE")
    );

    private static Integer safeInt(ResultSet rs, String c) throws java.sql.SQLException {
        int v = rs.getInt(c);
        return rs.wasNull() ? null : v;
    }
    private static LocalDateTime safeTs(ResultSet rs, String c) throws java.sql.SQLException {
        Timestamp t = rs.getTimestamp(c);
        return t == null ? null : t.toLocalDateTime();
    }

    public FirebirdProdutoDao(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") JdbcTemplate firebirdJdbcTemplate) {
        this.fb = firebirdJdbcTemplate;
    }

    /** Busca incremental por LASTUPDATE > :since, limitado por :limit. */
    public List<FbProduto> fetchAtualizados(LocalDateTime since, int limit) {
        String sql = """
            SELECT FIRST ? 
                   p.PRODUTO_ID, p.PRODUTO, p.APRESENTACAO, p.COD_BARRAS,
                   p.PROD_PRVENDA, p.PROD_PRPROMOCAO, p.PROD_SALDO, p.LASTUPDATE
            FROM   PRODUTOS p
            WHERE  (p.LASTUPDATE IS NULL OR p.LASTUPDATE > ?)
               AND p.PRODUTO IS NOT NULL
               AND p.PROD_PRVENDA > 0
            ORDER  BY COALESCE(p.LASTUPDATE, TIMESTAMP '1900-01-01') ASC
        """;
        return fb.query(sql, MAPPER, limit, since);
    }
}
