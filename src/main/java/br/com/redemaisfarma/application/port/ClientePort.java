package br.com.redemaisfarma.application.port;

import br.com.redemaisfarma.domain.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de aplicação para consulta/persistência de clientes via serviço externo. Implementada por ClienteHttpAdapter.
 */
public interface ClientePort {

    Optional<Cliente> buscarPorId(UUID id);

    Optional<Cliente> buscarPorCpf(String cpf);

    List<Cliente> buscarPorNome(String nome, int page, int size);

    Cliente salvarOuAtualizar(Cliente cliente);
}
