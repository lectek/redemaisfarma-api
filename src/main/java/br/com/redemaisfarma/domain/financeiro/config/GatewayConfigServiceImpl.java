/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.domain.financeiro.config;

import br.com.redemaisfarma.domain.financeiro.config.GatewayConfig;
import br.com.redemaisfarma.domain.financeiro.config.GatewayConfigRepository;
import br.com.redemaisfarma.domain.financeiro.config.GatewayConfigService;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GatewayConfigServiceImpl
implements GatewayConfigService {
    private final GatewayConfigRepository repo;

    public GatewayConfigServiceImpl(GatewayConfigRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly=true)
    public List<GatewayConfig> listar(Boolean onlyActive, String provider) {
        if (provider != null && !provider.isBlank()) {
            if (Boolean.TRUE.equals(onlyActive)) {
                return this.repo.findByAtivoAndProvedorIgnoreCaseOrderByAtualizadoEmDesc(true, provider);
            }
            if (Boolean.FALSE.equals(onlyActive)) {
                return this.repo.findByAtivoAndProvedorIgnoreCaseOrderByAtualizadoEmDesc(false, provider);
            }
            return this.repo.findByProvedorIgnoreCaseOrderByAtualizadoEmDesc(provider);
        }
        if (Boolean.TRUE.equals(onlyActive)) {
            return this.repo.findByAtivoOrderByAtualizadoEmDesc(true);
        }
        if (Boolean.FALSE.equals(onlyActive)) {
            return this.repo.findByAtivoOrderByAtualizadoEmDesc(false);
        }
        return this.repo.findAllByOrderByAtualizadoEmDesc();
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<GatewayConfig> buscarPorId(Long id) {
        return this.repo.findById(id);
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<GatewayConfig> buscarAtivaPorProvedor(String provider) {
        return this.repo.findFirstByProvedorIgnoreCaseAndAtivoTrue(provider);
    }

    @Override
    @Transactional
    public GatewayConfig criar(GatewayConfig nova) {
        nova.setId(null);
        this.validarObrigatorios(nova);
        this.validarUnicidade(nova.getProvedor(), nova.getNome(), null);
        if (nova.isAtivo()) {
            this.desativarAtivaAnterior(nova.getProvedor(), null);
        }
        return (GatewayConfig)this.repo.save(nova);
    }

    @Override
    @Transactional
    public GatewayConfig atualizar(Long id, GatewayConfig alterada) {
        GatewayConfig atual = (GatewayConfig)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Config n\u00e3o encontrada: id=" + id));
        this.validarObrigatorios(alterada);
        this.validarUnicidade(alterada.getProvedor(), alterada.getNome(), id);
        atual.setNome(alterada.getNome());
        atual.setProvedor(alterada.getProvedor());
        atual.setApiKey(alterada.getApiKey());
        atual.setApiSecret(alterada.getApiSecret());
        atual.setWebhookUrl(alterada.getWebhookUrl());
        atual.setTimeoutMs(alterada.getTimeoutMs());
        atual.setMaxRetries(alterada.getMaxRetries());
        atual.setMetadata(alterada.getMetadata());
        if (alterada.isAtivo() && !atual.isAtivo()) {
            this.desativarAtivaAnterior(atual.getProvedor(), id);
            atual.setAtivo(true);
        } else if (!alterada.isAtivo() && atual.isAtivo()) {
            atual.setAtivo(false);
        }
        return (GatewayConfig)this.repo.save(atual);
    }

    @Override
    @Transactional
    public void remover(Long id) {
        GatewayConfig cfg = (GatewayConfig)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Config n\u00e3o encontrada: id=" + id));
        if (cfg.isAtivo()) {
            throw new IllegalStateException("N\u00e3o \u00e9 permitido remover a configura\u00e7\u00e3o ATIVA do provedor " + cfg.getProvedor());
        }
        this.repo.deleteById(id);
    }

    @Override
    @Transactional
    public GatewayConfig ativar(Long id, boolean ativo) {
        GatewayConfig target = (GatewayConfig)this.repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Config n\u00e3o encontrada: id=" + id));
        if (ativo) {
            if (!target.isAtivo()) {
                this.desativarAtivaAnterior(target.getProvedor(), id);
                target.setAtivo(true);
                target = (GatewayConfig)this.repo.save(target);
            }
        } else if (target.isAtivo()) {
            target.setAtivo(false);
            target = (GatewayConfig)this.repo.save(target);
        }
        return target;
    }

    private void validarObrigatorios(GatewayConfig c) {
        if (c.getProvedor() == null || c.getProvedor().isBlank()) {
            throw new IllegalArgumentException("Provedor \u00e9 obrigat\u00f3rio");
        }
        if (c.getNome() == null || c.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome \u00e9 obrigat\u00f3rio");
        }
        if (c.getApiKey() == null || c.getApiKey().isBlank()) {
            throw new IllegalArgumentException("API Key \u00e9 obrigat\u00f3ria");
        }
    }

    private void validarUnicidade(String provedor, String nome, Long currentId) {
        boolean exists;
        String p = provedor == null ? null : provedor.toLowerCase(Locale.ROOT);
        String n = nome == null ? null : nome.toLowerCase(Locale.ROOT);
        boolean bl = exists = currentId == null ? this.repo.existsByProvedorIgnoreCaseAndNomeIgnoreCase(p, n) : this.repo.existsByProvedorIgnoreCaseAndNomeIgnoreCaseAndIdNot(p, n, currentId);
        if (exists) {
            throw new IllegalStateException("J\u00e1 existe configura\u00e7\u00e3o com esse Nome para o provedor " + provedor);
        }
    }

    private void desativarAtivaAnterior(String provedor, Long exceptId) {
        this.repo.findFirstByProvedorIgnoreCaseAndAtivoTrue(provedor).ifPresent(old -> {
            if (exceptId == null || !old.getId().equals(exceptId)) {
                old.setAtivo(false);
                this.repo.save(old);
            }
        });
    }
}

