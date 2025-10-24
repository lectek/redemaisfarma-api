/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.kafka.core.KafkaTemplate
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.outbound.messaging;

import br.com.redemaisfarma.adapters.outbound.messaging.ProductImagePublisher;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="kafka", name={"enabled"}, havingValue="true", matchIfMissing=false)
public class ProductImageEventPublisher
implements ProductImagePublisher {
    private static final Logger log = LoggerFactory.getLogger(ProductImageEventPublisher.class);
    private final KafkaTemplate<String, Object> kafka;
    private final String topic;

    public ProductImageEventPublisher(KafkaTemplate<String, Object> kafka, @Value(value="${app.kafka.topics.product-image-requested:product.image.requested}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    @Override
    public void publish(ProductImageRequestedEvent productImageRequestedEvent) {
        throw new Error("Unresolved compilation problem: \n\tThe method send(String, Object) in the type KafkaTemplate<String,Object> is not applicable for the arguments (String, Object, Object)\n");
    }
}

