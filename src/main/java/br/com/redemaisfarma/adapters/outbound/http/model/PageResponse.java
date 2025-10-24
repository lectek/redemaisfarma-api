/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.http.model;

import br.com.redemaisfarma.adapters.outbound.http.model.PageMetadata;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PageResponse<T>
implements Serializable {
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
        return this.content;
    }

    public void setContent(List<T> content) {
        this.content = content != null ? content : Collections.emptyList();
    }

    public PageMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(PageMetadata metadata) {
        this.metadata = metadata != null ? metadata : new PageMetadata();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageResponse)) {
            return false;
        }
        PageResponse that = (PageResponse)o;
        return Objects.equals(this.content, that.content) && Objects.equals(this.metadata, that.metadata);
    }

    public int hashCode() {
        return Objects.hash(this.content, this.metadata);
    }

    public String toString() {
        return "PageResponse{contentSize=" + (this.content == null ? 0 : this.content.size()) + ", metadata=" + String.valueOf(this.metadata) + "}";
    }
}

