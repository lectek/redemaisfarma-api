/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.annotation.Primary
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
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
public class EstoqueServiceImpl
implements EstoqueService {
    private static final Logger log = LoggerFactory.getLogger(EstoqueServiceImpl.class);
    private final JdbcTemplate jdbc;

    public EstoqueServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly=true)
    public boolean temDisponivel(Long produtoId, int quantidade) {
        boolean ok;
        Integer saldo = (Integer)this.jdbc.query("SELECT prod_saldo FROM produtos WHERE produto_id = ?", ps -> ps.setLong(1, produtoId), rs -> rs.next() ? Integer.valueOf(rs.getInt(1)) : null);
        boolean bl = ok = saldo != null && saldo >= quantidade;
        if (log.isDebugEnabled()) {
            log.debug("Estoque.temDisponivel produtoId={} solicitado={}, saldo={}, ok={}", new Object[]{produtoId, quantidade, saldo, ok});
        }
        return ok;
    }

    @Override
    @Transactional
    public void baixar(Long produtoId, int quantidade, String motivo) {
        int updated = this.jdbc.update("UPDATE produtos SET prod_saldo = prod_saldo - ? WHERE produto_id = ? AND prod_saldo >= ?", new Object[]{quantidade, produtoId, quantidade});
        if (updated == 0) {
            throw new IllegalStateException("Estoque insuficiente para produto " + produtoId + " (qtd=" + quantidade + ")");
        }
        this.jdbc.update("INSERT INTO movimento_estoque (produto_id, quantidade, tipo, motivo) VALUES (?, ?, 'SAIDA', ?)", new Object[]{produtoId, quantidade, motivo});
        if (log.isDebugEnabled()) {
            log.debug("Estoque.baixar OK produtoId={} quantidade={} motivo={}", new Object[]{produtoId, quantidade, motivo});
        }
    }

    public void ajustarSaldo(Long produtoId, int delta) {
        int updated;
        if (delta == 0) {
            return;
        }
        String tipo = delta < 0 ? "SAIDA" : "ENTRADA";
        int abs = Math.abs(delta);
        int n = updated = delta < 0 ? this.jdbc.update("UPDATE produtos SET prod_saldo = prod_saldo - ? WHERE produto_id = ? AND prod_saldo >= ?", new Object[]{abs, produtoId, abs}) : this.jdbc.update("UPDATE produtos SET prod_saldo = prod_saldo + ? WHERE produto_id = ?", new Object[]{abs, produtoId});
        if (updated == 0) {
            throw new IllegalStateException("Ajuste de saldo falhou para produto " + produtoId + " (delta=" + delta + ")");
        }
        this.jdbc.update("INSERT INTO movimento_estoque (produto_id, quantidade, tipo, motivo) VALUES (?, ?, ?, ?)", new Object[]{produtoId, abs, tipo, "AJUSTE"});
        if (log.isDebugEnabled()) {
            log.debug("Estoque.ajustarSaldo OK produtoId={} delta={}", (Object)produtoId, (Object)delta);
        }
    }
}

