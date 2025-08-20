package br.com.redemaisfarma.adapters.outbound.http.model;

import java.io.Serializable;
import java.util.Objects;

/** Metadados de paginação genéricos usados por vários clientes HTTP. */
public class PageMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    private int page; // página atual (0-based)
    private int size; // tamanho da página
    private long totalElements; // total de registros
    private int totalPages; // total de páginas

    public PageMetadata() {
    }

    public PageMetadata(int page, int size, long totalElements, int totalPages) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PageMetadata))
            return false;
        PageMetadata that = (PageMetadata) o;
        return page == that.page && size == that.size && totalElements == that.totalElements
                && totalPages == that.totalPages;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, totalElements, totalPages);
    }

    @Override
    public String toString() {
        return "PageMetadata{page=" + page + ", size=" + size + ", totalElements=" + totalElements + ", totalPages="
                + totalPages + '}';
    }
}
