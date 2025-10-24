/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.http.model;

import java.io.Serializable;
import java.util.Objects;

public class PageMetadata
implements Serializable {
    private static final long serialVersionUID = 1L;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PageMetadata() {
    }

    public PageMetadata(int page, int size, long totalElements, int totalPages) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return this.totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageMetadata)) {
            return false;
        }
        PageMetadata that = (PageMetadata)o;
        return this.page == that.page && this.size == that.size && this.totalElements == that.totalElements && this.totalPages == that.totalPages;
    }

    public int hashCode() {
        return Objects.hash(this.page, this.size, this.totalElements, this.totalPages);
    }

    public String toString() {
        return "PageMetadata{page=" + this.page + ", size=" + this.size + ", totalElements=" + this.totalElements + ", totalPages=" + this.totalPages + "}";
    }
}

