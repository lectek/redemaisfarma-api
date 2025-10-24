/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.kafka.core.KafkaTemplate
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.outbound.messaging.kafka.producer;

import br.com.redemaisfarma.adapters.outbound.messaging.kafka.model.PedidoCreatedEvent;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(value={KafkaTemplate.class})
@ConditionalOnProperty(prefix="kafka", name={"enabled"}, havingValue="true", matchIfMissing=false)
public class PedidoEventProducer {
    private static final Logger log;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PedidoEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        throw new Error("Unresolved compilation problems: \n\tThe method send(String, Object) in the type KafkaTemplate<String,Object> is not applicable for the arguments (String, Object, EventEnvelope<PedidoCreatedEvent>)\n\tThe method getMessage() is undefined for the type Object\n\tThe method getRecordMetadata() is undefined for the type Object\n\tThe method send(String, Object) in the type KafkaTemplate<String,Object> is not applicable for the arguments (String, Object, EventEnvelope<PedidoCreatedEvent>)\n");
    }

    public void shPedidoCreated(String string, PedidoCreatedEvent pedidoCreatedEvent) {
        throw new Error("Unresolved compilation problems: \n\tThe method send(String, Object) in the type KafkaTemplate<String,Object> is not applicable for the arguments (String, Object, EventEnvelope<PedidoCreatedEvent>)\n\tThe method getMessage() is undefined for the type Object\n\tThe method getRecordMetadata() is undefined for the type Object\n");
    }

    public void shPedidoCreatedQuiet(String string, PedidoCreatedEvent pedidoCreatedEvent) {
        throw new Error("Unresolved compilation problem: \n\tThe method send(String, Object) in the type KafkaTemplate<String,Object> is not applicable for the arguments (String, Object, EventEnvelope<PedidoCreatedEvent>)\n");
    }
}

