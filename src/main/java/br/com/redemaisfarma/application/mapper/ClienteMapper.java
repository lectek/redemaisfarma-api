package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.application.dto.request.ClienteRequestDTO;
import br.com.redemaisfarma.application.dto.response.ClienteResponseDTO;
import br.com.redemaisfarma.domain.model.ClienteModel;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ClienteMapper {

    // CREATE: copia só o que existe no Model; id é gerado pelo banco.
    @BeanMapping(ignoreByDefault = true)
    @Mappings({ @Mapping(target = "nome", source = "nome"), @Mapping(target = "cpf", source = "cpf"),
            @Mapping(target = "email", source = "email"), @Mapping(target = "telefone", source = "telefone"),
            @Mapping(target = "endereco", source = "endereco")
    // dataNascimento não existe no request -> permanece null
    })
    ClienteModel toModel(ClienteRequestDTO dto);

    // UPDATE parcial: não sobrescreve com null; mapeia só campos existentes em ambas as classes.
    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({ @Mapping(target = "nome", source = "nome"), @Mapping(target = "cpf", source = "cpf"),
            @Mapping(target = "email", source = "email"), @Mapping(target = "telefone", source = "telefone"),
            @Mapping(target = "endereco", source = "endereco")
    // idem: dataNascimento não vem no DTO
    })
    void update(@MappingTarget ClienteModel target, ClienteRequestDTO dto);

    // MODEL -> RESPONSE: só o que o Model fornece.
    @BeanMapping(ignoreByDefault = true)
    @Mappings({ @Mapping(target = "clienteId", source = "id"), @Mapping(target = "nome", source = "nome"),
            @Mapping(target = "cpf", source = "cpf"), @Mapping(target = "telefone", source = "telefone"),
            @Mapping(target = "endereco", source = "endereco")
    // grupoCliente, dataCadastro, dataAtualizacao, tenantId, traceId não existem no Model -> ignorados
    })
    ClienteResponseDTO toResponseDTO(ClienteModel model);

    List<ClienteResponseDTO> toResponseDTOList(List<ClienteModel> models);
}
