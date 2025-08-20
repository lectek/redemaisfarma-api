package br.com.redemaisfarma.adapters.outbound.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Contêiner genérico de paginação para respostas de serviços externos. Tente mapear os campos no WebClient para este
 * formato comum.
 *
 * Ex.: serviços que devolvem "items/total" podem ser convertidos no client.
 */
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Conteúdo da página. */
    @JsonProperty("content")
    private List<T> content;

    /** Índice da página atual (0-based). */
    @JsonProperty("page")
    private Integer page;

    /** Tamanho da página. */
    @JsonProperty("size")
    private Integer size;

    /** Total de páginas. */
    @JsonProperty("totalPages")
    private Integer totalPages;

    /** Total de elementos. */
    @JsonProperty("totalElements")
    private Long totalElements;

    /** Critérios de ordenação aplicados (ex.: ["nome,ASC"]). */
    @JsonProperty("sort")
    private List<String> sort;

    /** Flags de navegação. */
    @JsonProperty("hasNext")
    private Boolean hasNext;

    @JsonProperty("hasPrevious")
    private Boolean hasPrevious;

    public PageResponse() {
    }

    public PageResponse(List<T> content, Integer page, Integer size, Integer totalPages, Long totalElements,
            List<String> sort, Boolean hasNext, Boolean hasPrevious) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.sort = sort;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

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

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public List<String> getSort() {
        return sort;
    }

    public void setSort(List<String> sort) {
        this.sort = sort;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PageResponse))
            return false;
        PageResponse<?> that = (PageResponse<?>) o;
        return Objects.equals(content, that.content) && Objects.equals(page, that.page)
                && Objects.equals(size, that.size) && Objects.equals(totalPages, that.totalPages)
                && Objects.equals(totalElements, that.totalElements) && Objects.equals(sort, that.sort)
                && Objects.equals(hasNext, that.hasNext) && Objects.equals(hasPrevious, that.hasPrevious);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, page, size, totalPages, totalElements, sort, hasNext, hasPrevious);
    }

    @Override
    public String toString() {
        return "PageResponse{" + "content=" + (content == null ? 0 : content.size()) + ", page=" + page + ", size="
                + size + ", totalPages=" + totalPages + ", totalElements=" + totalElements + ", sort=" + sort
                + ", hasNext=" + hasNext + ", hasPrevious=" + hasPrevious + '}';
    }
}
