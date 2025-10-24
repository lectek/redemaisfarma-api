/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.kafka.common.serialization.Deserializer
 *  org.apache.kafka.common.serialization.StringDeserializer
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
 *  org.springframework.kafka.core.ConsumerFactory
 *  org.springframework.kafka.core.DefaultKafkaConsumerFactory
 *  org.springframework.kafka.support.serializer.JsonDeserializer
 */
package br.com.redemaisfarma.adapters.inbound.messaging.kafka.config;

import br.com.redemaisfarma.adapters.inbound.messaging.kafka.model.PedidoCreatedEventPayLoad;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, PedidoCreatedEventPayLoad> pedidoConsumerFactory() {
        Map<String, Object> props = Map.of("bootstrap.servers", "localhost:9092", "key.deserializer", StringDeserializer.class, "value.deserializer", JsonDeserializer.class, "spring.json.trusted.packages", "br.com.redemaisfarma.*", "spring.json.use.type.headers", false, "spring.json.value.default.type", PedidoCreatedEventPayLoad.class.getName());
        return new DefaultKafkaConsumerFactory(props, (Deserializer)new StringDeserializer(), (Deserializer)new JsonDeserializer(PedidoCreatedEventPayLoad.class, false));
    }

    @Bean(name={"pedidoListenerContainerFactory"})
    public ConcurrentKafkaListenerContainerFactory<String, PedidoCreatedEventPayLoad> pedidoListenerContainerFactory(ConsumerFactory<String, PedidoCreatedEventPayLoad> cf) {
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(cf);
        return factory;
    }
}


