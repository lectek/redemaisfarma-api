package br.com.redemaisfarma.application.service;

public interface VendaRapidaService {
    /** Cria um pedido rápido: valida cliente, produto, estoque; baixa estoque; retorna ID do pedido. */
    Long criar(String refCliente, String refProduto, int qtd);
}
