package br.com.redemaisfarma.domain.financeiro.config;

import java.util.List;
import java.util.Optional;

/**
 * Porta do domínio para gerenciar configurações de gateway.
 * A implementação pode estar no módulo de aplicação (ex.: um Service que usa Repository).
 */
public interface GatewayConfigService {

    /**
     * Lista todas as configurações, com filtros simples opcionais.
     *
     * @param onlyActive se true, retorna somente ativos; se null, ignora filtro
     * @param provider   filtra por provedor (case-insensitive); se null, ignora filtro
     */
    List<GatewayConfig> listar(Boolean onlyActive, String provider);

    Optional<GatewayConfig> buscarPorId(Long id);

    /**
     * Busca a configuração ativa para um provedor específico.
     * @param provider identificador do provedor (ex.: "pagarme")
     */
    Optional<GatewayConfig> buscarAtivaPorProvedor(String provider);

    /**
     * Cria nova configuração.
     * Implementações devem validar unicidade (provedor + nome) e regras de negócio.
     */
    GatewayConfig criar(GatewayConfig nova);

    /**
     * Atualiza a configuração existente.
     * Implementações devem manter integridade (ex.: não permitir dois ativos exclusivos,
     * se isso fizer parte da sua regra) e auditar mudanças sensíveis (apiKey/secret).
     */
    GatewayConfig atualizar(Long id, GatewayConfig alterada);

    /**
     * Remove uma configuração.
     * Implementações podem impedir a remoção se houver vínculo operacional.
     */
    void remover(Long id);

    /**
     * Ativa/Desativa uma configuração de forma idempotente.
     */
    GatewayConfig ativar(Long id, boolean ativo);
}
