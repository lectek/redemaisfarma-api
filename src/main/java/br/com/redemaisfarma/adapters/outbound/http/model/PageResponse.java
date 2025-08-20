package br.com.redemaisfarma.adapters.outbound.http.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Contêiner de paginação genérico. Use em qualquer client HTTP. (Independente do DTO específico de cada integração.)
 */
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> content;
    private PageMetadata metadata;

    public PageResponse() {
        this.content = Collections.emptyList();
        this.metadata = new PageMetadata();
    }

    public PageResponse(List<T> content, PageMetadata metadata) {
        this.content = content != null ? content : Collections.emptyList();
        this.metadata = metadata != null ? metadata : new PageMetadata();
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content != null ? content : Collections.emptyList();
    }

    public PageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PageMetadata metadata) {
        this.metadata = metadata != null ? metadata : new PageMetadata();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PageResponse))
            return false;
        PageResponse<?> that = (PageResponse<?>) o;
        return Objects.equals(content, that.content) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, metadata);
    }

    @Override
    public String toString() {
        return "PageResponse{contentSize=" + (content == null ? 0 : content.size()) + ", metadata=" + metadata + '}';
    }
}
