package br.com.redemaisfarma.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Envelope de resposta paginada para clientes. Ajuste os nomes das propriedades caso o provedor externo use outras
 * chaves (ex.: "items" em vez de "content").
 */
public class ClientePageExternal implements Serializable {

    @JsonProperty("content")
    private List<ClienteExternal> content;

    @JsonProperty("page")
    private PageMetadataExternal page;

    public List<ClienteExternal> getContent() {
        return content;
    }

    public void setContent(List<ClienteExternal> content) {
        this.content = content;
    }

    public PageMetadataExternal getPage() {
        return page;
    }

    public void setPage(PageMetadataExternal page) {
        this.page = page;
    }
}
