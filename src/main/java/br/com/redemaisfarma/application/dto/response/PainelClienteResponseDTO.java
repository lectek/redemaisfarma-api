package br.com.redemaisfarma.application.dto.response;

/**
 * Modelo de dados usado para exibir informaÃƒÂ§ÃƒÂµes no painel do cliente apÃƒÂ³s o login. Este objeto nÃƒÂ£o estÃƒÂ¡ ligado
 * diretamente ao banco de dados e ÃƒÂ© usado apenas para visualizaÃƒÂ§ÃƒÂ£o.
 */
public class PainelClienteResponseDTO {

    private String nomeCliente;
    private int pedidosRecentes;
    private double totalGasto;
    private String mensagemBoasVindas;
    private String categoriaMaisComprada;
    private boolean statusVip;

    /**
     * Construtor completo.
     */
    public PainelClienteResponseDTO(String nomeCliente, int pedidosRecentes, double totalGasto, String mensagemBoasVindas,
            String categoriaMaisComprada, boolean statusVip) {
        this.nomeCliente = nomeCliente;
        this.pedidosRecentes = pedidosRecentes;
        this.totalGasto = totalGasto;
        this.mensagemBoasVindas = mensagemBoasVindas;
        this.categoriaMaisComprada = categoriaMaisComprada;
        this.statusVip = statusVip;
    }

    // Getters e Setters

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public int getPedidosRecentes() {
        return pedidosRecentes;
    }

    public void setPedidosRecentes(int pedidosRecentes) {
        this.pedidosRecentes = pedidosRecentes;
    }

    public double getTotalGasto() {
        return totalGasto;
    }

    public void setTotalGasto(double totalGasto) {
        this.totalGasto = totalGasto;
    }

    public String getMensagemBoasVindas() {
        return mensagemBoasVindas;
    }

    public void setMensagemBoasVindas(String mensagemBoasVindas) {
        this.mensagemBoasVindas = mensagemBoasVindas;
    }

    public String getCategoriaMaisComprada() {
        return categoriaMaisComprada;
    }

    public void setCategoriaMaisComprada(String categoriaMaisComprada) {
        this.categoriaMaisComprada = categoriaMaisComprada;
    }

    public boolean isStatusVip() {
        return statusVip;
    }

    public void setStatusVip(boolean statusVip) {
        this.statusVip = statusVip;
    }

    @Override
    public String toString() {
        return "PainelClienteResponseDTO{" + "nomeCliente='" + nomeCliente + '\'' + ", pedidosRecentes=" + pedidosRecentes
                + ", totalGasto=" + totalGasto + ", mensagemBoasVindas='" + mensagemBoasVindas + '\''
                + ", categoriaMaisComprada='" + categoriaMaisComprada + '\'' + ", statusVip=" + statusVip + '}';
    }
}

