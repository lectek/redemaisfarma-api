package br.com.redemaisfarma.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Metadados de paginação comuns em APIs (page/size/total). Mantém a camada outbound desacoplada do Spring Page.
 */
public class PageMetadataExternal implements Serializable {

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("size")
    private Integer size;

    @JsonProperty("totalElements")
    private Long totalElements;

    @JsonProperty("totalPages")
    private Integer totalPages;

    @JsonProperty("sorted")
    private Boolean sorted;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getSorted() {
        return sorted;
    }

    public void setSorted(Boolean sorted) {
        this.sorted = sorted;
    }
}
