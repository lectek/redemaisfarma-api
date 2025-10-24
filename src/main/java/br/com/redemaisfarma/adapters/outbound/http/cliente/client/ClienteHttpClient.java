/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Qualifier
 *  org.springframework.util.LinkedMultiValueMap
 *  org.springframework.util.MultiValueMap
 *  org.springframework.web.reactive.function.client.WebClient
 */
package br.com.redemaisfarma.adapters.outbound.http.cliente.client;

import br.com.redemaisfarma.adapters.outbound.http.cliente.client.ClienteClient;
import br.com.redemaisfarma.adapters.outbound.http.cliente.config.ClienteClientProperties;
import br.com.redemaisfarma.adapters.outbound.http.cliente.model.ClienteExternal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

public class ClienteHttpClient
implements ClienteClient {
    private final WebClient clienteWebClient;
    private final ClienteClientProperties props;

    public ClienteHttpClient(@Qualifier(value="clienteWebClient") WebClient clienteWebClient, ClienteClientProperties props) {
        this.clienteWebClient = Objects.requireNonNull(clienteWebClient);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public Optional<ClienteExternal> getById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return this.exchangeGet("/clientes/{id}", Map.of("id", id.toString()), null, ClienteExternal.class);
    }

    @Override
    public Optional<ClienteExternal> getByCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return Optional.empty();
        }
        LinkedMultiValueMap q = new LinkedMultiValueMap();
        q.add((Object)"cpf", (Object)cpf);
        return this.exchangeGet("/clientes/by-cpf", null, (MultiValueMap<String, String>)q, ClienteExternal.class);
    }

    @Override
    public List<ClienteExternal> searchByName(String nome, int page, int size) {
        LinkedMultiValueMap q = new LinkedMultiValueMap();
        if (nome != null && !nome.isBlank()) {
            q.add((Object)"nome", (Object)nome);
        }
        q.add((Object)"page", (Object)String.valueOf(Math.max(page, 0)));
        q.add((Object)"size", (Object)String.valueOf(Math.max(size, 1)));
        ClienteExternal[] arr = this.exchangeGet("/clientes/search", null, (MultiValueMap<String, String>)q, ClienteExternal[].class).orElse(new ClienteExternal[0]);
        return Arrays.stream(arr).collect(Collectors.toList());
    }

    @Override
    public ClienteExternal upsert(ClienteExternal clienteExternal) {
        throw new Error("Unresolved compilation problem: \n\tThe method defaultIfEmpty(String) in the type Mono<String> is not applicable for the arguments (Object)\n");
    }

    private <T> Optional<T> exchangeGet(String string, Map<String, String> map, MultiValueMap<String, String> multiValueMap, Class<T> clazz) {
        throw new Error("Unresolved compilation problem: \n\tThe method defaultIfEmpty(String) in the type Mono<String> is not applicable for the arguments (Object)\n");
    }
}

