package br.com.redemaisfarma.adapters.outbound.messaging.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties props, ObjectMapper mapper) {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        cfg.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        cfg.put(ProducerConfig.CLIENT_ID_CONFIG, props.getClientId() + "-prod-" + UUID.randomUUID());
        cfg.put(ProducerConfig.ACKS_CONFIG, props.getAcks());
        cfg.put(ProducerConfig.RETRIES_CONFIG, props.getRetries());
        cfg.put(ProducerConfig.BATCH_SIZE_CONFIG, props.getBatchSize());
        cfg.put(ProducerConfig.LINGER_MS_CONFIG, props.getLingerMs());
        cfg.put(ProducerConfig.BUFFER_MEMORY_CONFIG, props.getBufferMemory());
        cfg.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, props.isIdempotence());
        // evita adicionar tipo nas headers (deixa o payload puro JSON)
        cfg.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        if (props.getExtra() != null) {
            cfg.putAll(props.getExtra());
        }
        return new DefaultKafkaProducerFactory<>(cfg);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }

    /** Caso não exista um ObjectMapper HTTP já registrado. */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapperFallback() {
        return new ObjectMapper();
    }
}
