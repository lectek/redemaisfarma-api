package br.com.redemaisfarma.adapters.outbound.messaging.kafka.producer;

import br.com.redemaisfarma.adapters.outbound.messaging.kafka.model.EventEnvelope;
import br.com.redemaisfarma.adapters.outbound.messaging.kafka.model.PedidoCreatedEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
public class PedidoEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PedidoEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
    }

    /**
     * Publica um evento de criação de pedido usando o envelope padrão.
     *
     * @param tenantId
     *            inquilino (pode ser null)
     * @param event
     *            payload do evento
     */
    public void shPedidoCreated(String tenantId, PedidoCreatedEvent event) {
        // Monte o envelope conforme sua classe EventEnvelope<T>
        EventEnvelope<PedidoCreatedEvent> envelope = new EventEnvelope<>("PedidoCreated", "pedido-service", tenantId,
                event);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(KafkaTopics.PEDIDO_CREATED,
                event.getPedidoId().toString(), // chave
                envelope // valor
        );

        // Callback moderno no CompletableFuture
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Falha ao publicar PedidoCreated (key={}): {}", event.getPedidoId(), ex.getMessage(), ex);
                return;
            }
            RecordMetadata meta = result.getRecordMetadata();
            log.info("PedidoCreated publicado em topic={} partition={} offset={}", meta.topic(), meta.partition(),
                    meta.offset());
        });
    }
}
