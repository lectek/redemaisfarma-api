package br.com.redemaisfarma.adapters.outbound.http.mapper;

import br.com.redemaisfarma.adapters.outbound.http.cliente.model.EnderecoExternal;
import br.com.redemaisfarma.domain.Endereco;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper de endereço (HTTP externo <-> domínio).
 */
public final class EnderecoExternalMapper {

    private EnderecoExternalMapper() {
    }

    /* =============== External -> Domain =============== */
    public static Endereco toDomain(EnderecoExternal ext) {
        if (ext == null)
            return null;

        Endereco e = new Endereco();
        // NÃO há id no contrato externo → não setamos id aqui

        e.setCep(ext.getCep());
        e.setLogradouro(ext.getLogradouro());
        e.setNumero(ext.getNumero());
        e.setComplemento(ext.getComplemento());
        e.setBairro(ext.getBairro());
        e.setCidade(ext.getCidade());
        // No externo é "estado"; no domínio normalmente é "uf"
        e.setUf(ext.getEstado());
        e.setPrincipal(Boolean.TRUE.equals(ext.getPrincipal()));
        return e;
    }

    /* =============== Domain -> External =============== */
    public static EnderecoExternal toExternal(Endereco dom) {
        if (dom == null)
            return null;

        EnderecoExternal ext = new EnderecoExternal();
        // NÃO há setId no externo

        ext.setCep(dom.getCep());
        ext.setLogradouro(dom.getLogradouro());
        ext.setNumero(dom.getNumero());
        ext.setComplemento(dom.getComplemento());
        ext.setBairro(dom.getBairro());
        ext.setCidade(dom.getCidade());
        // Domínio usa "uf" → externo usa "estado"
        ext.setEstado(dom.getUf());
        ext.setPrincipal(dom.isPrincipal());
        return ext;
    }

    public static List<Endereco> toDomain(List<EnderecoExternal> list) {
        if (list == null || list.isEmpty())
            return Collections.emptyList();
        return list.stream().filter(Objects::nonNull).map(EnderecoExternalMapper::toDomain)
                .collect(Collectors.toList());
    }

    public static List<EnderecoExternal> toExternal(List<Endereco> list) {
        if (list == null || list.isEmpty())
            return Collections.emptyList();
        return list.stream().filter(Objects::nonNull).map(EnderecoExternalMapper::toExternal)
                .collect(Collectors.toList());
    }
}
