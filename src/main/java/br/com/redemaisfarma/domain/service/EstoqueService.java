// src/main/java/br/com/redemaisfarma/domain/service/EstoqueService.java
package br.com.redemaisfarma.domain.service;

public interface EstoqueService {
    boolean temDisponivel(Long produtoId, int qtd);
    void baixar(Long produtoId, int qtd, String motivo);
}
