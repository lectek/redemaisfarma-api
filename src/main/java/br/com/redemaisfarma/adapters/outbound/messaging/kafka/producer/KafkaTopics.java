package br.com.redemaisfarma.adapters.outbound.messaging.kafka.producer;

/** Centraliza nomes de tópicos para evitar hardcode espalhado. */
public final class KafkaTopics {
    private KafkaTopics() {
    }

    public static final String PEDIDO_CREATED = "pedido.created.v1";
    // adicione outros aqui (ex.: PEDIDO_STATUS_CHANGED, CLIENTE_UPDATED, etc.)
}
