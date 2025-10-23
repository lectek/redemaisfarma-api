// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/entity/ProdutoStatus.java
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

/** Status do ciclo de vida do produto. */
public enum ProdutoStatus {
    IMPORTADO,   // recém-chegado do export, aguardando revisão
    VALIDADO,    // revisado pelo admin, mas ainda não publicado
    PUBLICADO    // já liberado para aparecer na loja
}
