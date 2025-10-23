package br.com.redemaisfarma.domain.financeiro.config;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GatewayConfigRepository extends JpaRepository<GatewayConfig, Long> {

    // ativa exclusiva por provedor (case-insensitive)
    Optional<GatewayConfig> findFirstByProvedorIgnoreCaseAndAtivoTrue(String provedor);

    // último registro por provedor
    Optional<GatewayConfig> findTopByProvedorIgnoreCaseOrderByAtualizadoEmDesc(String provedor);

    // listagens
    List<GatewayConfig> findByProvedorIgnoreCaseOrderByAtualizadoEmDesc(String provedor);
    List<GatewayConfig> findByAtivoAndProvedorIgnoreCaseOrderByAtualizadoEmDesc(boolean ativo, String provedor);
    List<GatewayConfig> findByAtivoOrderByAtualizadoEmDesc(boolean ativo);
    List<GatewayConfig> findAllByOrderByAtualizadoEmDesc();

    // unicidade (provedor + nome)
    boolean existsByProvedorIgnoreCaseAndNomeIgnoreCase(String provedor, String nome);
    boolean existsByProvedorIgnoreCaseAndNomeIgnoreCaseAndIdNot(String provedor, String nome, Long id);
}
