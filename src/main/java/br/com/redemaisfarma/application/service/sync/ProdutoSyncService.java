/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service.sync;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.mapper.ProdutoLegacyManualMapper;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name={"legacy.sync.enabled"}, havingValue="true", matchIfMissing=false)
public class ProdutoSyncService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(ProdutoSyncService.class);
    private static final int PAGE_SIZE = 500;
    private final ProdutoLegacyRepository legacyRepository;
    private final ProdutoRepository novoRepository;
    private final ProdutoLegacyManualMapper mapper;

    @Transactional
    public int sincronizar() {
        return this.sincronizarProdutos();
    }

    @Transactional
    public int sincronizarProdutos() {
        throw new Error("Unresolved compilation problem: \n\tType mismatch: cannot convert from element type Object to ProdutoLegacyEntity\n");
    }

    private ProdutoEntity localizarExistente(ProdutoLegacyEntity legacy, ProdutoEntity mapeado) {
        Optional<ProdutoEntity> byCb;
        Optional<ProdutoEntity> byLegacy;
        if (legacy != null && legacy.getId() != null && (byLegacy = this.novoRepository.findByLegacyId(legacy.getId().longValue())).isPresent()) {
            return byLegacy.get();
        }
        if (!this.isVazio(mapeado.getCodigoBarras()) && (byCb = this.novoRepository.findByCodigoBarras(mapeado.getCodigoBarras())).isPresent()) {
            return byCb.get();
        }
        ProdutoEntity novo = new ProdutoEntity();
        if (legacy != null && legacy.getId() != null) {
            novo.setLegacyId(legacy.getId().longValue());
        }
        return novo;
    }

    private void mesclarDados(ProdutoEntity destino, ProdutoEntity src) {
        if (!this.isVazio(src.getNome())) {
            destino.setNome(src.getNome());
        }
        if (!this.isVazio(src.getDescricao())) {
            destino.setDescricao(src.getDescricao());
        }
        if (src.getPrecoVenda() != null) {
            destino.setPrecoVenda(src.getPrecoVenda());
        }
        if (!this.isVazio(src.getImagem())) {
            destino.setImagem(src.getImagem());
        }
        if (!this.isVazio(src.getCategoria())) {
            destino.setCategoria(src.getCategoria());
        }
        if (!this.isVazio(src.getCodigoBarras())) {
            destino.setCodigoBarras(src.getCodigoBarras());
        }
        if (src.getPrecoCusto() != null) {
            destino.setPrecoCusto(src.getPrecoCusto());
        }
        if (src.getDisponivel() != null) {
            destino.setDisponivel(src.getDisponivel());
        }
        if (!this.isVazio(src.getFabricante())) {
            destino.setFabricante(src.getFabricante());
        }
        if (src.getCodigoOriginal() != null) {
            destino.setCodigoOriginal(src.getCodigoOriginal());
        }
        if (!this.isVazio(src.getUnidade())) {
            destino.setUnidade(src.getUnidade());
        }
        if (src.getDataCadastro() != null) {
            destino.setDataCadastro(src.getDataCadastro());
        }
        if (src.getLegacyId() != null) {
            destino.setLegacyId(src.getLegacyId());
        }
        destino.setEstoque(Math.max(0, src.getEstoque()));
    }

    private void aplicarDefaultsENormalizacoes(ProdutoEntity e) {
        if (e.getImagem() != null && e.getImagem().isBlank()) {
            e.setImagem(null);
        }
        if (e.getDisponivel() == null) {
            e.setDisponivel(Boolean.TRUE);
        }
        if (e.getEstoque() < 0) {
            e.setEstoque(0);
        }
        if (e.getPrecoVenda() == null || this.menorOuIgualZero(e.getPrecoVenda())) {
            e.setPrecoVenda(new BigDecimal("0.01"));
        }
    }

    private boolean menorOuIgualZero(BigDecimal v) {
        return v.compareTo(BigDecimal.ZERO) <= 0;
    }

    private boolean isVazio(String s) {
        return s == null || s.isBlank();
    }

    private String hashDoLegacy(ProdutoLegacyEntity l) {
        block3: {
            try {
                if (l != null) break block3;
                return null;
            }
            catch (Exception e) {
                log.debug("N\u00e3o foi poss\u00edvel calcular hash do legado: {}", (Object)e.getMessage());
                return null;
            }
        }
        String base = String.join((CharSequence)"|", String.valueOf(l.getId()), this.safe(l.getNome()), this.safe(l.getCodigoBarras()), this.safe(l.getPrecoVenda()), this.safe(l.getPrecoPromocao()), this.safe(l.getSaldo()), this.safe(l.getMargemLucro()), this.safe(l.getInicioPromocao()), this.safe(l.getTerminoPromocao()));
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(base.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    @Generated
    public ProdutoSyncService(ProdutoLegacyRepository legacyRepository, ProdutoRepository novoRepository, ProdutoLegacyManualMapper mapper) {
        this.legacyRepository = legacyRepository;
        this.novoRepository = novoRepository;
        this.mapper = mapper;
    }
}

