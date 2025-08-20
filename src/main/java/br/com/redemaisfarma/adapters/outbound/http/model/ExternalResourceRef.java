package br.com.redemaisfarma.adapters.outbound.http.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Referência genérica a um recurso externo retornado por integrações HTTP. Útil para payloads leves: mantém
 * identificação e rótulo.
 */
public class ExternalResourceRef implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;

    public ExternalResourceRef() {
    }

    public ExternalResourceRef(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ExternalResourceRef))
            return false;
        ExternalResourceRef that = (ExternalResourceRef) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "ExternalResourceRef{id=" + id + ", name='" + name + "'}";
    }
}
