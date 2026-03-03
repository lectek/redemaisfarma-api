/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Index
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.PreUpdate
 *  jakarta.persistence.Table
 *  jakarta.persistence.Version
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name="produto", indexes={@Index(name="idx_produto_codigo_barras", columnList="codigo_barras"), @Index(name="idx_produto_legacy_id", columnList="legacy_id")})
public class ProdutoEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="nome", nullable=false)
    private String nome;
    @Column(name="descricao")
    private String descricao;
    @Column(name="preco_venda", precision=15, scale=2)
    private BigDecimal precoVenda;
    @Column(name="preco_custo", precision=15, scale=2)
    private BigDecimal precoCusto;
    @Column(name="preco_promocional", precision=15, scale=2)
    private BigDecimal precoPromocional;
    @Column(name="imagem")
    private String imagem;
    @Column(name="imagem_webp")
    private String imagemWebp;
    @Column(name="categoria", nullable=false)
    private String categoria;
    @Enumerated(value=EnumType.STRING)
    @Column(name="tarja_medicacao", length=32)
    private TarjaMedicacao tarjaMedicacao;
    @Column(name="exige_receita", nullable=false)
    private Boolean exigeReceita;
    @Column(name="codigo_barras")
    private String codigoBarras;
    @Enumerated(value=EnumType.STRING)
    @Column(name="metodo_leitura_codigo_barras", length=32)
    private MetodoLeituraCodigoBarras metodoLeituraCodigoBarras;
    @Column(name="codigo_original")
    private Long codigoOriginal;
    @Column(name="unidade")
    private String unidade;
    @Column(name="estoque")
    private Integer estoque;
    @Column(name="disponivel")
    private Boolean disponivel;
    @Column(name="fabricante")
    private String fabricante;
    @Column(name="data_cadastro")
    private LocalDate dataCadastro;
    @Column(name="data_importacao")
    private LocalDateTime dataImportacao;
    @Column(name="publicado_em")
    private LocalDateTime publicadoEm;
    @Column(name="despublicado_em")
    private LocalDateTime despublicadoEm;
    @Column(name="ordem_carrossel")
    private Integer ordemCarrossel;
    @Column(name="destaque_carrossel")
    private Boolean destaqueCarrossel;
    @Column(name="legacy_id")
    private Long legacyId;
    @Column(name="id_produto_externo")
    private Long idProdutoExterno;
    @Column(name="hash_legado", nullable=false, length=64)
    private String hashLegado;
    @Column(name="status_sync")
    private String statusSync;
    @Column(name="desconto_percentual")
    private Integer descontoPercentual;
    @Enumerated(value=EnumType.STRING)
    @Column(name="status")
    private ProdutoStatus status;
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
    @Column(name="validador")
    private String validador;
    @Version
    @Column(name="version", nullable=false)
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.categoria == null) {
            this.categoria = "Sem Categoria";
        }
        if (this.statusSync == null) {
            this.statusSync = "SINCRONIZADO";
        }
        if (this.status == null) {
            this.status = ProdutoStatus.IMPORTADO;
        }
        if (this.exigeReceita == null) {
            this.exigeReceita = Boolean.FALSE;
        }
        if (this.metodoLeituraCodigoBarras == null) {
            this.metodoLeituraCodigoBarras = MetodoLeituraCodigoBarras.DESCONHECIDO;
        }
        this.garantirHashLegado();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.garantirHashLegado();
    }

    private void garantirHashLegado() {
        if (this.hashLegado == null || this.hashLegado.isBlank()) {
            String base = (this.legacyId != null ? this.legacyId.toString() : "") + "|" + (this.codigoBarras != null ? this.codigoBarras : "") + "|" + (this.nome != null ? this.nome : "");
            this.hashLegado = ProdutoEntity.sha256Hex(base);
        }
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            byte[] byArray = d;
            int n = d.length;
            int n2 = 0;
            while (n2 < n) {
                byte b = byArray[n2];
                sb.append(String.format("%02x", b));
                ++n2;
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new IllegalStateException("Falha ao computar hash_legado", e);
        }
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoVenda() {
        return this.precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public BigDecimal getPrecoCusto() {
        return this.precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }

    public BigDecimal getPrecoPromocional() {
        return this.precoPromocional;
    }

    public void setPrecoPromocional(BigDecimal precoPromocional) {
        this.precoPromocional = precoPromocional;
    }

    public String getImagem() {
        return this.imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getImagemWebp() {
        return this.imagemWebp;
    }

    public void setImagemWebp(String imagemWebp) {
        this.imagemWebp = imagemWebp;
    }

    public String getCategoria() {
        return this.categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public TarjaMedicacao getTarjaMedicacao() {
        return this.tarjaMedicacao;
    }

    public void setTarjaMedicacao(TarjaMedicacao tarjaMedicacao) {
        this.tarjaMedicacao = tarjaMedicacao;
    }

    public Boolean getExigeReceita() {
        return this.exigeReceita;
    }

    public void setExigeReceita(Boolean exigeReceita) {
        this.exigeReceita = exigeReceita;
    }

    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public MetodoLeituraCodigoBarras getMetodoLeituraCodigoBarras() {
        return this.metodoLeituraCodigoBarras;
    }

    public void setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras metodoLeituraCodigoBarras) {
        this.metodoLeituraCodigoBarras = metodoLeituraCodigoBarras;
    }

    public Long getCodigoOriginal() {
        return this.codigoOriginal;
    }

    public void setCodigoOriginal(Long codigoOriginal) {
        this.codigoOriginal = codigoOriginal;
    }

    public String getUnidade() {
        return this.unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public Integer getEstoque() {
        return this.estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Boolean getDisponivel() {
        return this.disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getFabricante() {
        return this.fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public LocalDate getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataImportacao() {
        return this.dataImportacao;
    }

    public void setDataImportacao(LocalDateTime dataImportacao) {
        this.dataImportacao = dataImportacao;
    }

    public LocalDateTime getPublicadoEm() {
        return this.publicadoEm;
    }

    public void setPublicadoEm(LocalDateTime publicadoEm) {
        this.publicadoEm = publicadoEm;
    }

    public LocalDateTime getDespublicadoEm() {
        return this.despublicadoEm;
    }

    public void setDespublicadoEm(LocalDateTime despublicadoEm) {
        this.despublicadoEm = despublicadoEm;
    }

    public Integer getOrdemCarrossel() {
        return this.ordemCarrossel;
    }

    public void setOrdemCarrossel(Integer ordemCarrossel) {
        this.ordemCarrossel = ordemCarrossel;
    }

    public Boolean getDestaqueCarrossel() {
        return this.destaqueCarrossel;
    }

    public void setDestaqueCarrossel(Boolean destaqueCarrossel) {
        this.destaqueCarrossel = destaqueCarrossel;
    }

    public Long getLegacyId() {
        return this.legacyId;
    }

    public void setLegacyId(Long legacyId) {
        this.legacyId = legacyId;
    }

    public Long getIdProdutoExterno() {
        return this.idProdutoExterno;
    }

    public void setIdProdutoExterno(Long idProdutoExterno) {
        this.idProdutoExterno = idProdutoExterno;
    }

    public String getHashLegado() {
        return this.hashLegado;
    }

    public void setHashLegado(String hashLegado) {
        this.hashLegado = hashLegado;
    }

    public String getStatusSync() {
        return this.statusSync;
    }

    public void setStatusSync(String statusSync) {
        this.statusSync = statusSync;
    }

    public Integer getDescontoPercentual() {
        return this.descontoPercentual;
    }

    public void setDescontoPercentual(Integer descontoPercentual) {
        this.descontoPercentual = descontoPercentual;
    }

    public ProdutoStatus getStatus() {
        return this.status;
    }

    public void setStatus(ProdutoStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getValidador() {
        return this.validador;
    }

    public void setValidador(String validador) {
        this.validador = validador;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProdutoEntity)) {
            return false;
        }
        ProdutoEntity that = (ProdutoEntity)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }
}
