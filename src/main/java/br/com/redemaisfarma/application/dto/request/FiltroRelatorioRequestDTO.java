package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name = "FiltroRelatorioRequestDTO", description = "Filtros para relatórios operacionais")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FiltroRelatorioRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do cliente", example = "123", required = true)
    @NotNull(message = "{filtroRelatorio.clienteId.notNull}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "ID da filial", example = "10", required = true)
    @NotNull(message = "{filtroRelatorio.filialId.notNull}")
    @JsonProperty("filialId")
    private Long filialId;

    @Schema(description = "Data inicial (inclusive)", type = "string", format = "date", example = "2025-01-01", required = true)
    @NotNull(message = "{filtroRelatorio.dataInicial.notNull}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @PastOrPresent(message = "{filtroRelatorio.dataInicial.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("dataInicial")
    private LocalDate dataInicial;

    @Schema(description = "Data final (inclusive)", type = "string", format = "date", example = "2025-12-31", required = true)
    @NotNull(message = "{filtroRelatorio.dataFinal.notNull}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @FutureOrPresent(message = "{filtroRelatorio.dataFinal.futureOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("dataFinal")
    private LocalDate dataFinal;

    @Schema(description = "Lista de categorias de produto para filtrar")
    @Size(max = 10, message = "{filtroRelatorio.categoriasProduto.size}")
    @JsonProperty("categoriasProduto")
    private List<@NotBlank(message = "{filtroRelatorio.categoriasProduto.notBlank}") @Size(max = 60, message = "{filtroRelatorio.categoriasProduto.item.size}") String> categoriasProduto;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "redemaisfarma-001", required = true)
    @NotBlank(message = "{filtroRelatorio.tenantId.notBlank}")
    @Size(max = 60, message = "{filtroRelatorio.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID) para logs e auditoria", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    @Schema(description = "Número da página (zero-based)", example = "0")
    @Min(value = 0, message = "{filtroRelatorio.page.min}")
    @JsonProperty("page")
    private Integer page = 0;

    @Schema(description = "Tamanho da página (1..1000)", example = "50")
    @Min(value = 1, message = "{filtroRelatorio.size.min}")
    @Max(value = 1000, message = "{filtroRelatorio.size.max}")
    @JsonProperty("size")
    private Integer size = 50;

    @Schema(description = "Critérios de ordenação no formato \"campo,ASC|DESC\" (ex.: dataInicial,DESC)")
    @Size(max = 5, message = "{filtroRelatorio.sort.size}")
    @JsonProperty("sort")
    private List<@Pattern(regexp = "^[a-zA-Z0-9_\\.]+,(ASC|DESC)$", message = "{filtroRelatorio.sort.pattern}") String> sort;

    // ------------ validações compostas (coerência entre campos) ------------
    @AssertTrue(message = "{filtroRelatorio.periodo.coerente}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isPeriodoValido() {
        if (dataInicial == null || dataFinal == null)
            return false;
        return !dataInicial.isAfter(dataFinal);
    }

    // ------------------------------- construtores ---------------------------
    public FiltroRelatorioRequestDTO() {
    }

    public FiltroRelatorioRequestDTO(Long clienteId, Long filialId, LocalDate dataInicial, LocalDate dataFinal,
            List<String> categoriasProduto, String tenantId, UUID traceId, Integer page, Integer size,
            List<String> sort) {
        this.clienteId = clienteId;
        this.filialId = filialId;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.categoriasProduto = categoriasProduto;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    // ------------------------------- getters/setters ------------------------
    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getFilialId() {
        return filialId;
    }

    public void setFilialId(Long filialId) {
        this.filialId = filialId;
    }

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(LocalDate dataFinal) {
        this.dataFinal = dataFinal;
    }

    public List<String> getCategoriasProduto() {
        return categoriasProduto;
    }

    public void setCategoriasProduto(List<String> categoriasProduto) {
        this.categoriasProduto = categoriasProduto;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
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

    public List<String> getSort() {
        return sort;
    }

    public void setSort(List<String> sort) {
        this.sort = sort;
    }

    // -------------------------------- utilitários ---------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FiltroRelatorioRequestDTO that))
            return false;
        return Objects.equals(clienteId, that.clienteId) && Objects.equals(filialId, that.filialId)
                && Objects.equals(dataInicial, that.dataInicial) && Objects.equals(dataFinal, that.dataFinal)
                && Objects.equals(categoriasProduto, that.categoriasProduto) && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(traceId, that.traceId) && Objects.equals(page, that.page)
                && Objects.equals(size, that.size) && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, filialId, dataInicial, dataFinal, categoriasProduto, tenantId, traceId, page,
                size, sort);
    }

    @Override
    public String toString() {
        return "FiltroRelatorioRequestDTO{" + "clienteId=" + clienteId + ", filialId=" + filialId + ", dataInicial="
                + dataInicial + ", dataFinal=" + dataFinal + ", categoriasProduto=" + categoriasProduto + ", tenantId='"
                + tenantId + '\'' + ", traceId=" + traceId + ", page=" + page + ", size=" + size + ", sort=" + sort
                + '}';
    }
}
