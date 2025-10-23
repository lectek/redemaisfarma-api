package br.com.redemaisfarma.domain.service.impl;

import br.com.redemaisfarma.domain.service.EstoqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
public class EstoqueServiceImpl implements EstoqueService {
    private static final Logger log = LoggerFactory.getLogger(EstoqueServiceImpl.class);
    private final JdbcTemplate jdbc;

    public EstoqueServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean temDisponivel(Long produtoId, int quantidade) {
        Integer saldo = jdbc.query(
                "SELECT prod_saldo FROM produtos WHERE produto_id = ?",
                ps -> ps.setLong(1, produtoId),
                rs -> rs.next() ? rs.getInt(1) : null
        );
        boolean ok = saldo != null && saldo >= quantidade;
        if (log.isDebugEnabled()) {
            log.debug("Estoque.temDisponivel produtoId={} solicitado={}, saldo={}, ok={}",
                    produtoId, quantidade, saldo, ok);
        }
        return ok;
    }

    @Override
    @Transactional
    public void baixar(Long produtoId, int quantidade, String motivo) {
        // Debita de forma atômica (só atualiza se houver saldo suficiente)
        int updated = jdbc.update(
                "UPDATE produtos SET prod_saldo = prod_saldo - ? " +
                "WHERE produto_id = ? AND prod_saldo >= ?",
                quantidade, produtoId, quantidade
        );
        if (updated == 0) {
            throw new IllegalStateException(
                    "Estoque insuficiente para produto " + produtoId + " (qtd=" + quantidade + ")"
            );
        }

        // Registra movimento
        jdbc.update(
                "INSERT INTO movimento_estoque (produto_id, quantidade, tipo, motivo) " +
                "VALUES (?, ?, 'SAIDA', ?)",
                produtoId, quantidade, motivo
        );

        if (log.isDebugEnabled()) {
            log.debug("Estoque.baixar OK produtoId={} quantidade={} motivo={}", produtoId, quantidade, motivo);
        }
    }

    /* Se você já tinha esse helper em outras partes do código, mantemos sem @Override (não faz parte da interface) */
    public void ajustarSaldo(Long produtoId, int delta) {
        if (delta == 0) return;
        String tipo = delta < 0 ? "SAIDA" : "ENTRADA";
        int abs = Math.abs(delta);

        int updated = (delta < 0)
                ? jdbc.update("UPDATE produtos SET prod_saldo = prod_saldo - ? WHERE produto_id = ? AND prod_saldo >= ?",
                              abs, produtoId, abs)
                : jdbc.update("UPDATE produtos SET prod_saldo = prod_saldo + ? WHERE produto_id = ?",
                              abs, produtoId);

        if (updated == 0) {
            throw new IllegalStateException("Ajuste de saldo falhou para produto " + produtoId + " (delta=" + delta + ")");
        }

        jdbc.update(
                "INSERT INTO movimento_estoque (produto_id, quantidade, tipo, motivo) VALUES (?, ?, ?, ?)",
                produtoId, abs, tipo, "AJUSTE"
        );
        if (log.isDebugEnabled()) {
            log.debug("Estoque.ajustarSaldo OK produtoId={} delta={}", produtoId, delta);
        }
    }
}
