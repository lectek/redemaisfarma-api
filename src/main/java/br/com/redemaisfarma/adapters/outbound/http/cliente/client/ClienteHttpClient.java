package br.com.redemaisfarma.adapters.outbound.http.cliente.client;

import br.com.redemaisfarma.adapters.outbound.http.cliente.config.ClienteClientProperties;
import br.com.redemaisfarma.adapters.outbound.http.cliente.exception.ClienteClientException;
import br.com.redemaisfarma.adapters.outbound.http.cliente.model.ClienteExternal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação baseada em WebClient (blocking via block()) para simplificar o consumo em camadas superiores. Se
 * desejar, podemos expor métodos reativos depois.
 */
public class ClienteHttpClient implements ClienteClient {

    private final WebClient clienteWebClient;
    @SuppressWarnings("unused")
    private final ClienteClientProperties props;

    public ClienteHttpClient(@Qualifier("clienteWebClient") WebClient clienteWebClient, ClienteClientProperties props) {
        this.clienteWebClient = Objects.requireNonNull(clienteWebClient);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public Optional<ClienteExternal> getById(UUID id) {
        if (id == null)
            return Optional.empty();
        return exchangeGet("/clientes/{id}", Map.of("id", id.toString()), null, ClienteExternal.class);
    }

    @Override
    public Optional<ClienteExternal> getByCpf(String cpf) {
        if (cpf == null || cpf.isBlank())
            return Optional.empty();
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        q.add("cpf", cpf);
        return exchangeGet("/clientes/by-cpf", null, q, ClienteExternal.class);
    }

    @Override
    public List<ClienteExternal> searchByName(String nome, int page, int size) {
        MultiValueMap<String, String> q = new LinkedMultiValueMap<>();
        if (nome != null && !nome.isBlank())
            q.add("nome", nome);
        q.add("page", String.valueOf(Math.max(page, 0)));
        q.add("size", String.valueOf(Math.max(size, 1)));

        ClienteExternal[] arr = exchangeGet("/clientes/search", null, q, ClienteExternal[].class)
                .orElse(new ClienteExternal[0]);
        return Arrays.stream(arr).collect(Collectors.toList());
    }

    @Override
    public ClienteExternal upsert(ClienteExternal payload) {
        if (payload == null)
            throw new IllegalArgumentException("payload não pode ser nulo");

        try {
            return clienteWebClient.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON).bodyValue(payload).retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), resp -> resp
                            .bodyToMono(String.class).defaultIfEmpty("Erro desconhecido")
                            .flatMap(msg -> Mono.error(new ClienteClientException(
                                    "Falha ao upsert cliente: HTTP " + resp.statusCode().value() + " - " + msg))))
                    .bodyToMono(ClienteExternal.class).block();
        } catch (ClienteClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClienteClientException("Erro de comunicação com serviço de clientes (upsert).", e);
        }
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    private <T> Optional<T> exchangeGet(String path, Map<String, String> pathVars, MultiValueMap<String, String> query,
            Class<T> bodyType) {
        try {
            WebClient.RequestHeadersSpec<?> spec = clienteWebClient.get().uri(uriBuilder -> {
                var b = uriBuilder.path(path);
                if (query != null && !query.isEmpty()) {
                    query.forEach((k, vs) -> vs.forEach(v -> b.queryParam(k, v)));
                }
                return b.build(pathVars == null ? Map.of() : pathVars);
            }).accept(MediaType.APPLICATION_JSON);

            return Optional.ofNullable(spec.retrieve().onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    resp -> resp.bodyToMono(String.class).defaultIfEmpty("Erro desconhecido")
                            .flatMap(msg -> Mono.error(new ClienteClientException(
                                    "Falha ao consultar clientes: HTTP " + resp.statusCode().value() + " - " + msg))))
                    .bodyToMono(bodyType).block());
        } catch (ClienteClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClienteClientException("Erro de comunicação com serviço de clientes (GET " + path + ").", e);
        }
    }
}
