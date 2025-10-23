package br.com.redemaisfarma.application.core.auth;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.CustomerEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Serviço de domínio para vincular/criar cliente local a partir de dados de provedores OAuth2.
 * - Procura por e-mail; se não existir, cria novo.
 * - Atualiza provider/providerUserId/avatar quando faltarem.
 * - Marca e-mail verificado quando vier do provedor.
 */
@Service
public class SocialCustomerService {

    private final CustomerRepository repo;

    public SocialCustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    /**
     * Upsert idempotente para dados de redes sociais.
     *
     * @param provider        "google" | "facebook"
     * @param providerUserId  id do usuário no provedor (sub/id)
     * @param email           e-mail (pode ser null se o provedor não retornar)
     * @param nome            nome para exibição
     * @param avatarUrl       url do avatar/foto (quando disponível)
     * @param emailVerificado true se o provedor confirmou verificação do e-mail
     */
    @Transactional
    public void linkOrCreate(String provider,
                             String providerUserId,
                             String email,
                             String nome,
                             String avatarUrl,
                             boolean emailVerificado) {

        CustomerEntity e = null;

        // 1) Tenta por e-mail (quando disponível)
        if (email != null && !email.isBlank()) {
            e = repo.findByEmail(email).orElse(null);
        }

        // 2) Se não achou por e-mail, tenta por (provider, providerUserId)
        if (e == null && provider != null && providerUserId != null) {
            e = repo.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);
        }

        // 3) Cria novo se não existir
        if (e == null) {
            e = new CustomerEntity();
            e.setNome(Objects.requireNonNullElse(nome, "Cliente"));
            e.setEmail(email);
            e.setAvatarUrl(avatarUrl);
            e.setProvider(provider);
            e.setProviderUserId(providerUserId);
            e.setEmailVerificado(emailVerificado || (email != null)); // heurística
            e.setAtivo(true);
            // se tiver campos como role/perfil, defina aqui um padrão:
            // e.setRole("CLIENTE");
            repo.save(e);
            return;
        }

        // 4) Se já existe, atualiza campos que estiverem vazios/desatualizados
        boolean changed = false;

        if (e.getProvider() == null && provider != null) {
            e.setProvider(provider);
            changed = true;
        }
        if (e.getProviderUserId() == null && providerUserId != null) {
            e.setProviderUserId(providerUserId);
            changed = true;
        }
        if (e.getAvatarUrl() == null && avatarUrl != null) {
            e.setAvatarUrl(avatarUrl);
            changed = true;
        }
        if (!e.isEmailVerificado() && emailVerificado) {
            e.setEmailVerificado(true);
            changed = true;
        }
        if (e.getNome() == null && nome != null) {
            e.setNome(nome);
            changed = true;
        }
        if (changed) {
            repo.save(e);
        }
    }
}
