/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  org.apache.kafka.common.serialization.Serializer
 *  org.apache.kafka.common.serialization.StringSerializer
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.kafka.core.DefaultKafkaProducerFactory
 *  org.springframework.kafka.core.KafkaTemplate
 *  org.springframework.kafka.core.ProducerFactory
 *  org.springframework.kafka.support.serializer.JsonSerializer
 */
package br.com.redemaisfarma.adapters.outbound.messaging.kafka.config;

import br.com.redemaisfarma.adapters.outbound.messaging.kafka.config.KafkaProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableConfigurationProperties(value={KafkaProperties.class})
@ConditionalOnProperty(prefix="kafka", name={"enabled"}, havingValue="true", matchIfMissing=false)
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties props, ObjectMapper mapper) {
        HashMap<String, Object> cfg = new HashMap<String, Object>();
        cfg.put("bootstrap.servers", props.getBootstrapServers());
        cfg.put("client.id", props.getClientId() + "-" + String.valueOf(UUID.randomUUID()));
        cfg.put("acks", props.getAcks());
        cfg.put("retries", props.getRetries());
        cfg.put("batch.size", props.getBatchSize());
        cfg.put("linger.ms", props.getLingerMs());
        cfg.put("buffer.memory", props.getBufferMemory());
        cfg.put("enable.idempotence", props.isIdempotence());
        cfg.put("spring.json.add.type.headers", false);
        if (props.getExtra() != null) {
            cfg.putAll(props.getExtra());
        }
        return new DefaultKafkaProducerFactory(cfg, (Serializer)new StringSerializer(), (Serializer)new JsonSerializer(mapper));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate(pf);
    }

    @Bean
    @ConditionalOnMissingBean(value={ObjectMapper.class})
    public ObjectMapper objectMapperFallback() {
        return new ObjectMapper();
    }
}

