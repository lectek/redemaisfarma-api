package br.com.redemaisfarma.adapters.outbound.http.cliente.client;

import br.com.redemaisfarma.adapters.outbound.http.cliente.model.ClienteExternal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de integração HTTP com o serviço de Cliente (externo). Retorna modelos do OUTBOUND (ClienteExternal). O
 * mapeamento para DTOs do application fica fora deste módulo.
 */
public interface ClienteClient {

    Optional<ClienteExternal> getById(UUID id);

    Optional<ClienteExternal> getByCpf(String cpf);

    List<ClienteExternal> searchByName(String nome, int page, int size);

    /**
     * Cria ou atualiza registro de cliente no sistema externo. Retorna o recurso (como persistido) pelo provedor
     * externo.
     */
    ClienteExternal upsert(ClienteExternal payload);
}
