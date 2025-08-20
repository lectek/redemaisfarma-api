package br.com.redemaisfarma.adapters.outbound.http.cliente.config;

import br.com.redemaisfarma.adapters.outbound.http.cliente.client.ClienteHttpClient;
import br.com.redemaisfarma.adapters.outbound.http.cliente.client.ClienteClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ClienteClientProperties.class)
public class ClienteClientConfig {

    /**
     * WebClient dedicado ao serviço de Cliente, baseado no builder comum. - Usa baseUrl vinda das properties - Define
     * Accept/Content-Type como JSON - Pode customizar codecs/limites por cliente
     */
    @Bean("clienteWebClient")
    public WebClient clienteWebClient(WebClient.Builder webClientBuilder, ClienteClientProperties props) {

        // Aumentar buffer se precisar lidar com payloads grandes
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(props.getMaxInMemorySize())).build();

        return webClientBuilder.clone() // não “contamina” outros clientes que usem o mesmo builder
                .baseUrl(props.getBaseUrl()).defaultHeaders(h -> {
                    h.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
                    h.setContentType(MediaType.APPLICATION_JSON);
                }).exchangeStrategies(strategies).build();
    }

    /**
     * Adapter/Client de domínio que usa o WebClient dedicado.
     */
    @Bean
    public ClienteClient clienteClient(@Qualifier("clienteWebClient") WebClient clienteWebClient,
            ClienteClientProperties props) {
        return new ClienteHttpClient(clienteWebClient, props);
    }
}
