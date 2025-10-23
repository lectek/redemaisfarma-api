// src/main/java/br/com/redemaisfarma/domain/sync/SyncStatus.java
package br.com.redemaisfarma.domain.sync;

public final class SyncStatus {
    private SyncStatus() {}
    public static final String SINCRONIZADO = "SINCRONIZADO";
    public static final String PENDENTE     = "PENDENTE";
    public static final String ERRO         = "ERRO";
    public static final String IGNORADO     = "IGNORADO";
}
