// src/main/java/br/com/redemaisfarma/application/mapper/PedidoMapper.java
package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.domain.Pedido;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface PedidoMapper {

    // Domain -> Entity (para CREATE; relacionamentos/auditoria ficam sob responsabilidade da entidade)
    @Mappings({
        @Mapping(target = "cliente", ignore = true),
        @Mapping(target = "itens", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    PedidoEntity toEntity(Pedido source);

    // Entity -> Domain
    Pedido toDomain(PedidoEntity entity);

    // Patch em entity (para UPDATE; não mexe em id/relacionamentos/auditoria)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "cliente", ignore = true),
        @Mapping(target = "itens", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    void updateEntity(@MappingTarget PedidoEntity target, Pedido source);
}
