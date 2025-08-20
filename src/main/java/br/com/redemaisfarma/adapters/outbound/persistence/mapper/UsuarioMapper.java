package br.com.redemaisfarma.adapters.outbound.persistence.mapper;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.domain.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper responsável por converter entre UsuarioEntity (persistência) e Usuario (domínio de negócio).
 */
@Component
public class UsuarioMapper {

    /**
     * Converte uma entidade JPA para o modelo de domínio.
     *
     * @param entity
     *            objeto persistido
     *
     * @return objeto de domínio
     */
    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null)
            return null;

        return new Usuario(entity.getId(), entity.getNome(), entity.getEmail(), entity.getCpf(), entity.getSenha(),
                entity.getRoles() != null ? List.copyOf(entity.getRoles()) : List.of());
    }

    /**
     * Converte um objeto de domínio para uma entidade JPA.
     *
     * @param usuario
     *            modelo de domínio
     *
     * @return entidade JPA
     */
    public UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null)
            return null;

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setNome(usuario.getNome());
        entity.setEmail(usuario.getEmail());
        entity.setCpf(usuario.getCpf());
        entity.setSenha(usuario.getSenha());
        entity.setRoles(usuario.getRoles());
        return entity;
    }

    /**
     * Converte uma lista de entidades para domínio.
     */
    public List<Usuario> toDomainList(List<UsuarioEntity> entities) {
        return entities == null ? List.of()
                : entities.stream().filter(Objects::nonNull).map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * Converte uma lista de domínios para entidades.
     */
    public List<UsuarioEntity> toEntityList(List<Usuario> dominios) {
        return dominios == null ? List.of()
                : dominios.stream().filter(Objects::nonNull).map(this::toEntity).collect(Collectors.toList());
    }
}
