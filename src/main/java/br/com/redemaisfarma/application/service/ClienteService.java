package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.domain.Cliente;
import java.util.List;

/**
 * Service de Cliente com API "oficial" em inglês e aliases em PT-BR
 * para compatibilidade retroativa com controllers antigos.
 */
public interface ClienteService {

    // ===== API oficial (use estes na implementação) =====
    Cliente findById(Long id);
    List<Cliente> list();
    Cliente create(Cliente cliente);
    Cliente update(Long id, Cliente cliente);
    void delete(Long id);

    // ===== ALIASES em PT-BR (compatibilidade) =====
    /** @deprecated prefira {@link #findById(Long)} */
    @Deprecated
    default Cliente buscarPorId(Long id) {
        return findById(id);
    }

    /** @deprecated prefira {@link #list()} */
    @Deprecated
    default List<Cliente> listarTodos() {
        return list();
    }

    /** @deprecated prefira {@link #create(Cliente)} */
    @Deprecated
    default Cliente salvar(Cliente cliente) {
        return create(cliente);
    }

    /** @deprecated prefira {@link #update(Long, Cliente)} */
    @Deprecated
    default Cliente atualizar(Long id, Cliente cliente) {
        return update(id, cliente);
    }

    /** @deprecated prefira {@link #delete(Long)} */
    @Deprecated
    default void deletar(Long id) {
        delete(id);
    }
}
