package br.com.redemaisfarma.adapters.outbound.legacy.mapper;

import br.com.redemaisfarma.application.dto.legacy.ProdutoLegacyDTO;
import br.com.redemaisfarma.application.dto.legacy.ProdutoNovoDTO;
import br.com.redemaisfarma.domain.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProdutoLegacyDTOMapper {

    // 🔁 ProdutoLegacyDTO → ProdutoNovoDTO
    @Mappings({ @Mapping(source = "id", target = "id"), @Mapping(source = "nome", target = "descricaoProd"),
            @Mapping(source = "codigoBarras", target = "codigoBarras"),
            @Mapping(source = "saldo", target = "estoqueAtual"), @Mapping(source = "precoVenda", target = "precoVenda"),
            @Mapping(source = "precoPromocao", target = "precoCusto"), // considerado como custo vindo do Firebird
            @Mapping(source = "margemLucro", target = "margemLucro"),
            @Mapping(source = "inicioPromocao", target = "inicioPromocao"),
            @Mapping(source = "terminoPromocao", target = "terminoPromocao"),
            @Mapping(source = "apresentacao", target = "unidade"),
            @Mapping(source = "precoAnterior", target = "precoAnterior"), @Mapping(source = "bonus", target = "bonus"),

            // Campos que virão futuramente de outros serviços ou tabelas
            @Mapping(target = "categoria", ignore = true), @Mapping(target = "ativo", ignore = true),
            @Mapping(target = "fornecedor", ignore = true), @Mapping(target = "fabricante", ignore = true),
            @Mapping(target = "dataCadastro", ignore = true), @Mapping(target = "imagem", ignore = true) })
    ProdutoNovoDTO toProdutoNovoDTO(ProdutoLegacyDTO legacyDTO);

    // 🔁 ProdutoNovoDTO → ProdutoLegacyDTO
    @Mappings({ @Mapping(source = "id", target = "id"), @Mapping(source = "descricaoProd", target = "nome"),
            @Mapping(source = "codigoBarras", target = "codigoBarras"),
            @Mapping(source = "estoqueAtual", target = "saldo"), @Mapping(source = "precoVenda", target = "precoVenda"),
            @Mapping(source = "precoCusto", target = "precoPromocao"),
            @Mapping(source = "margemLucro", target = "margemLucro"),
            @Mapping(source = "inicioPromocao", target = "inicioPromocao"),
            @Mapping(source = "terminoPromocao", target = "terminoPromocao"),
            @Mapping(source = "unidade", target = "apresentacao"),
            @Mapping(source = "precoAnterior", target = "precoAnterior"), @Mapping(source = "bonus", target = "bonus"),

            // Campos que só existem no legado
            @Mapping(target = "estoqueMinimo", ignore = true), @Mapping(target = "fornecedorId", ignore = true),
            @Mapping(target = "categoriaId", ignore = true), @Mapping(target = "comissaoId", ignore = true) })
    ProdutoLegacyDTO toProdutoLegacyDTO(ProdutoNovoDTO novoDTO);

    // 🔁 ProdutoLegacyDTO → Produto (modelo de domínio)
    @Mappings({ @Mapping(source = "id", target = "id"), @Mapping(source = "nome", target = "descricao"),
            @Mapping(source = "codigoBarras", target = "codigoBarras"),
            @Mapping(source = "precoVenda", target = "precoVenda"),

            // Descartáveis ou não usados no domínio puro
            @Mapping(target = "nome", ignore = true), @Mapping(target = "imagem", ignore = true) })
    Produto toDomain(ProdutoLegacyDTO legacyDTO);
}
