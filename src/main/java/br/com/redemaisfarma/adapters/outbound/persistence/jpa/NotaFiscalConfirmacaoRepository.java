// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/jpa/NotaFiscalConfirmacaoRepository.java
package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.NotaFiscalConfirmacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaFiscalConfirmacaoRepository extends JpaRepository<NotaFiscalConfirmacaoEntity, Long> {
}
