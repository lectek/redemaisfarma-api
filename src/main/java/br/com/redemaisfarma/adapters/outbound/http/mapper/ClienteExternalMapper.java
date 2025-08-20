package br.com.redemaisfarma.adapters.outbound.http.mapper;

import br.com.redemaisfarma.adapters.outbound.http.cliente.model.ClienteExternal;
import br.com.redemaisfarma.domain.Cliente;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ClienteExternalMapper {

    private ClienteExternalMapper() {
    }

    /* ========================= External -> Domain ========================= */

    public static Cliente toDomain(ClienteExternal ext) {
        if (ext == null)
            return null;

        Cliente c = new Cliente();

        // NÃO setamos o ID porque o externo é UUID e o domínio usa Long com IDENTITY
        // c.setId(...);

        c.setNome(ext.getNome());
        c.setCpf(ext.getCpf());
        c.setEmail(ext.getEmail());
        c.setTelefone(ext.getTelefone());
        // Externo: getDataNascimento()
        c.setDataDeNascimento(ext.getDataNascimento());
        // Boolean -> boolean
        c.setAtivo(Boolean.TRUE.equals(ext.getAtivo()));
        return c;
    }

    public static Optional<Cliente> toDomain(Optional<ClienteExternal> extOpt) {
        return extOpt.map(ClienteExternalMapper::toDomain);
    }

    public static List<Cliente> toDomain(List<ClienteExternal> list) {
        if (list == null || list.isEmpty())
            return Collections.emptyList();
        return list.stream().filter(Objects::nonNull).map(ClienteExternalMapper::toDomain).collect(Collectors.toList());
    }

    /* ========================= Domain -> External ========================= */

    public static ClienteExternal toExternal(Cliente dom) {
        if (dom == null)
            return null;

        ClienteExternal ext = new ClienteExternal();

        // Se o ClienteExternal tiver ID opcional como UUID, só defina aqui se você realmente tiver esse valor.
        // ext.setId(...);

        ext.setNome(dom.getNome());
        ext.setCpf(dom.getCpf());
        ext.setEmail(dom.getEmail());
        ext.setTelefone(dom.getTelefone());
        // Domínio: getDataDeNascimento()
        ext.setDataNascimento(dom.getDataDeNascimento());
        ext.setAtivo(dom.isAtivo());
        return ext;
    }

    public static List<ClienteExternal> toExternal(List<Cliente> list) {
        if (list == null || list.isEmpty())
            return Collections.emptyList();
        return list.stream().filter(Objects::nonNull).map(ClienteExternalMapper::toExternal)
                .collect(Collectors.toList());
    }
}
