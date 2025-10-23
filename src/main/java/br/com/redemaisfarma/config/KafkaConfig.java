// src/main/java/br/com/redemaisfarma/config/KafkaConfig.java
package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@EnableKafka
@Configuration
public class KafkaConfig {

  // Alias para o factory padrão criado pelo Spring Boot
  @Bean(name = "pedidoKafkaListenerContainerFactory")
  public ConcurrentKafkaListenerContainerFactory<?, ?> pedidoKafkaListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory
  ) {
    // Se quiser rodar sem Kafka no dev:
    // kafkaListenerContainerFactory.setAutoStartup(false);
    return kafkaListenerContainerFactory;
  }
}
