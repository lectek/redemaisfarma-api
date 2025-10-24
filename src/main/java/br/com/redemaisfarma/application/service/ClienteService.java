/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.domain.Cliente
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.domain.Cliente;
import java.util.List;

public interface ClienteService {
    public Cliente findById(Long var1);

    public List<Cliente> list();

    public Cliente create(Cliente var1);

    public Cliente update(Long var1, Cliente var2);

    public void delete(Long var1);

    @Deprecated
    default public Cliente buscarPorId(Long id) {
        return this.findById(id);
    }

    @Deprecated
    default public List<Cliente> listarTodos() {
        return this.list();
    }

    @Deprecated
    default public Cliente salvar(Cliente cliente) {
        return this.create(cliente);
    }

    @Deprecated
    default public Cliente atualizar(Long id, Cliente cliente) {
        return this.update(id, cliente);
    }

    @Deprecated
    default public void deletar(Long id) {
        this.delete(id);
    }
}

