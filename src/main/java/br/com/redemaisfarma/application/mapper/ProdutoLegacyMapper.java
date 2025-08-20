package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.application.dto.legacy.ProdutoLegacyDTO;
import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProdutoLegacyMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "codigoBarras", target = "codigoBarras")
    @Mapping(source = "saldo", target = "saldo")
    @Mapping(source = "precoVenda", target = "precoVenda")
    @Mapping(source = "precoPromocao", target = "precoPromocao")
    @Mapping(source = "estoqueMinimo", target = "estoqueMinimo")
    @Mapping(source = "margemLucro", target = "margemLucro")
    @Mapping(source = "inicioPromocao", target = "inicioPromocao")
    @Mapping(source = "terminoPromocao", target = "terminoPromocao") // ✅ Adicionada esta linha
    @Mapping(source = "bonus", target = "bonus")
    @Mapping(source = "apresentacao", target = "apresentacao")
    @Mapping(source = "precoAnterior", target = "precoAnterior")
    @Mapping(source = "fornecedorId", target = "fornecedorId")
    @Mapping(source = "categoriaId", target = "categoriaId")
    @Mapping(source = "comissaoId", target = "comissaoId")
    ProdutoLegacyDTO toDto(ProdutoLegacyEntity entity);

    @InheritInverseConfiguration
    ProdutoLegacyEntity toEntity(ProdutoLegacyDTO dto);

    // 🔁 Conversão de listas
    List<ProdutoLegacyDTO> toDtoList(List<ProdutoLegacyEntity> entities);

    List<ProdutoLegacyEntity> toEntityList(List<ProdutoLegacyDTO> dtos);
}
