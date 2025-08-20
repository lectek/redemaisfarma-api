package br.com.redemaisfarma.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo utilizado para exibir dados resumidos no painel administrativo da loja. ContÃ©m mÃ©tricas de desempenho,
 * indicadores de pedidos, produtos e clientes.
 */
public class PainelAdminResponseDTO {

    private String adminNome;
    private int totalPedidos;
    private double totalLucro;
    private double ticketMedio;
    private int clientesAtivos;
    private int qtdProdutos;
    private int qtdPedidosPendentes;
    private int qtdPedidosEntregues;
    private double satisfacaoCliente;
    private LocalDateTime dataUltimoPedido;
    private List<String> categoriasMaisVendidas;
    private List<String> alertas;

    // Getters e Setters

    public String getAdminNome() {
        return adminNome;
    }

    public void setAdminNome(String adminNome) {
        this.adminNome = adminNome;
    }

    public int getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(int totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public double getTotalLucro() {
        return totalLucro;
    }

    public void setTotalLucro(double totalLucro) {
        this.totalLucro = totalLucro;
    }

    public double getTicketMedio() {
        return ticketMedio;
    }

    public void setTicketMedio(double ticketMedio) {
        this.ticketMedio = ticketMedio;
    }

    public int getClientesAtivos() {
        return clientesAtivos;
    }

    public void setClientesAtivos(int clientesAtivos) {
        this.clientesAtivos = clientesAtivos;
    }

    public int getQtdProdutos() {
        return qtdProdutos;
    }

    public void setQtdProdutos(int qtdProdutos) {
        this.qtdProdutos = qtdProdutos;
    }

    public int getQtdPedidosPendentes() {
        return qtdPedidosPendentes;
    }

    public void setQtdPedidosPendentes(int qtdPedidosPendentes) {
        this.qtdPedidosPendentes = qtdPedidosPendentes;
    }

    public int getQtdPedidosEntregues() {
        return qtdPedidosEntregues;
    }

    public void setQtdPedidosEntregues(int qtdPedidosEntregues) {
        this.qtdPedidosEntregues = qtdPedidosEntregues;
    }

    public double getSatisfacaoCliente() {
        return satisfacaoCliente;
    }

    public void setSatisfacaoCliente(double satisfacaoCliente) {
        this.satisfacaoCliente = satisfacaoCliente;
    }

    public LocalDateTime getDataUltimoPedido() {
        return dataUltimoPedido;
    }

    public void setDataUltimoPedido(LocalDateTime dataUltimoPedido) {
        this.dataUltimoPedido = dataUltimoPedido;
    }

    public List<String> getCategoriasMaisVendidas() {
        return categoriasMaisVendidas;
    }

    public void setCategoriasMaisVendidas(List<String> categoriasMaisVendidas) {
        this.categoriasMaisVendidas = categoriasMaisVendidas;
    }

    public List<String> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<String> alertas) {
        this.alertas = alertas;
    }
}

