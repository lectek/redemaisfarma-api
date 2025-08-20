package br.com.redemaisfarma.application.port.outbound;

import java.util.Optional;
import br.com.redemaisfarma.domain.Usuario;

/**
 * Porta de saída para autenticação e acesso ao repositório de usuários. Define os contratos que o repositório precisa
 * cumprir para autenticação.
 */
public interface AuthRepositoryPort {

    /**
     * Busca um usuário pelo identificador de login (e-mail, CPF ou nome de usuário).
     *
     * @param identifier
     *            identificador de login
     *
     * @return Optional com o usuário encontrado, ou vazio se não existir
     */
    Optional<Usuario> findByIdentifier(String identifier);

    /**
     * Verifica se o usuário está bloqueado por excesso de tentativas de login falhas.
     *
     * @param identifier
     *            entrada de login
     *
     * @return true se o usuário estiver bloqueado
     */
    boolean isBlocked(String identifier);

    /**
     * Registra uma tentativa de login mal-sucedida.
     *
     * @param identifier
     *            login utilizado
     */
    void registerFailedAttempt(String identifier);

    /**
     * Reseta o contador de falhas após login com sucesso.
     *
     * @param identifier
     *            login utilizado
     */
    void resetFailedAttempts(String identifier);

    /**
     * Atualiza a data/hora do último acesso.
     *
     * @param userId
     *            ID do usuário
     */
    boolean updateLastAccess(Long userId);

    /**
     * Salva ou atualiza os dados do usuário.
     *
     * @param user
     *            objeto do domínio que representa o usuário
     */
    void save(Usuario user);

    /**
     * Verifica se um usuário existe pelo e-mail.
     *
     * @param email
     *            e-mail a ser verificado
     *
     * @return true se o e-mail existir
     */
    boolean existsByEmail(String email);

    /**
     * Autentica um usuário com identificador e senha.
     *
     * @param identifier
     *            e-mail, CPF ou nome de usuário
     * @param password
     *            senha fornecida
     *
     * @return o usuário autenticado
     */
    Usuario authenticate(String identifier, String password);

    /**
     * Verifica se o usuário é cliente VIP.
     *
     * @param identifier
     *            CPF ou e-mail
     *
     * @return true se for VIP
     */
    boolean isClienteVip(String identifier);
}
