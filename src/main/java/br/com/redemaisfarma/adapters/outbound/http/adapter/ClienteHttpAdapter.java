package br.com.redemaisfarma.adapters.outbound.http.adapter;

import br.com.redemaisfarma.adapters.outbound.http.cliente.client.ClienteClient;
import br.com.redemaisfarma.adapters.outbound.http.cliente.model.ClienteExternal;
import br.com.redemaisfarma.adapters.outbound.http.mapper.ClienteExternalMapper;
import br.com.redemaisfarma.domain.Cliente;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter HTTP que conversa com o serviço externo de clientes e traduz para o domínio interno.
 *
 * Implementa a porta de aplicação (ClientePort). Ajuste o import da interface da porta conforme o seu projeto:
 * br.com.redemaisfarma.application.port.ClientePort (exemplo)
 */
@Component
public class ClienteHttpAdapter implements br.com.redemaisfarma.application.port.ClientePort {

    private final ClienteClient client;

    public ClienteHttpAdapter(ClienteClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return client.getById(id).map(ClienteExternalMapper::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return client.getByCpf(cpf).map(ClienteExternalMapper::toDomain);
    }

    @Override
    public List<Cliente> buscarPorNome(String nome, int page, int size) {
        List<ClienteExternal> list = client.searchByName(nome, page, size);
        return list.stream().map(ClienteExternalMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Cliente salvarOuAtualizar(Cliente cliente) {
        if (cliente == null)
            throw new IllegalArgumentException("cliente não pode ser nulo");
        ClienteExternal payload = ClienteExternalMapper.toExternal(cliente);
        ClienteExternal saved = client.upsert(payload);
        return ClienteExternalMapper.toDomain(saved);
    }
}
