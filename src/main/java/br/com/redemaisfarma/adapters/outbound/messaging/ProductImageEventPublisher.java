package br.com.redemaisfarma.adapters.outbound.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
public class ProductImageEventPublisher implements ProductImagePublisher {

    private static final Logger log = LoggerFactory.getLogger(ProductImageEventPublisher.class);

    private final KafkaTemplate<String, Object> kafka;
    private final String topic;

    public ProductImageEventPublisher(KafkaTemplate<String, Object> kafka,
                                      @Value("${app.kafka.topics.product-image-requested:product.image.requested}")
                                      String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    @Override
    public void publish(ProductImageRequestedEvent event) {
        String key = event.productId() != null ? event.productId().toString() : null;
        kafka.send(topic, key, event);
        log.info("Kafka publish -> topic={}, key={}, event={}", topic, key, event);
    }
}
