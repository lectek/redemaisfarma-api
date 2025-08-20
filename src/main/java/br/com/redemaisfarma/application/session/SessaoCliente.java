package br.com.redemaisfarma.application.session;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Representa os dados da sessÃ£o do cliente autenticado.
 */
public class SessaoCliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nome;
    private String email;
    private boolean clienteVip;
    private LocalDateTime dataUltimoAcesso;

    // Construtor vazio (necessÃ¡rio para serializaÃ§Ã£o/deserializaÃ§Ã£o)
    public SessaoCliente() {
    }

    // Construtor completo
    public SessaoCliente(Long id, String nome, String email, boolean clienteVip, LocalDateTime dataUltimoAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.clienteVip = clienteVip;
        this.dataUltimoAcesso = dataUltimoAcesso;
    }

    // âœ… Construtor alternativo (usado no LoginController)
    public SessaoCliente(Long id, String nome, String email, LocalDateTime dataUltimoAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.dataUltimoAcesso = dataUltimoAcesso;
        this.clienteVip = false; // padrÃ£o se nÃ£o for informado
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isClienteVip() {
        return clienteVip;
    }

    public void setClienteVip(boolean clienteVip) {
        this.clienteVip = clienteVip;
    }

    public LocalDateTime getDataUltimoAcesso() {
        return dataUltimoAcesso;
    }

    public void setDataUltimoAcesso(LocalDateTime dataUltimoAcesso) {
        this.dataUltimoAcesso = dataUltimoAcesso;
    }

    // MÃ©todo utilitÃ¡rio
    public boolean isClienteLogado() {
        return this.id != null;
    }
}

