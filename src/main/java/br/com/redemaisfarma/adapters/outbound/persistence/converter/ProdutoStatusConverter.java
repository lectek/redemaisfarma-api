// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/converter/ProdutoStatusConverter.java
package br.com.redemaisfarma.adapters.outbound.persistence.converter;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProdutoStatusConverter implements AttributeConverter<ProdutoStatus, String> {

    private static final String DB_IMPORTADO = "DRAFT";      // <--- ajuste
    private static final String DB_VALIDADO  = "APPROVED";   // <--- ajuste
    private static final String DB_PUBLICADO = "PUBLISHED";  // <--- ajuste

    @Override
    public String convertToDatabaseColumn(ProdutoStatus status) {
        if (status == null) return null;
        return switch (status) {
            case IMPORTADO -> DB_IMPORTADO;
            case VALIDADO  -> DB_VALIDADO;
            case PUBLICADO -> DB_PUBLICADO;
        };
    }

    @Override
    public ProdutoStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        String v = dbValue.trim().toUpperCase();
        // aceita tanto pt-BR quanto os tokens do banco
        return switch (v) {
            case "IMPORTADO", DB_IMPORTADO -> ProdutoStatus.IMPORTADO;
            case "VALIDADO",  DB_VALIDADO  -> ProdutoStatus.VALIDADO;
            case "PUBLICADO", DB_PUBLICADO -> ProdutoStatus.PUBLICADO;
            default -> throw new IllegalArgumentException("Status inválido no banco: " + dbValue);
        };
    }
}
