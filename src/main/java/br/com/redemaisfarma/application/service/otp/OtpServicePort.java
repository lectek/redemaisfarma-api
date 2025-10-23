// src/main/java/br/com/redemaisfarma/application/service/otp/OtpServicePort.java
package br.com.redemaisfarma.application.service.otp;

public interface OtpServicePort {

    /**
     * Resultado do start de OTP.
     */
    record StartResult(
            String deliveryId,
            String maskedDestino,
            int cooldownSec,
            int ttlSeconds,
            String demoCode // sempre null em prod
    ) {}

    /**
     * Exceções funcionais do fluxo de OTP.
     */
    class OtpException extends RuntimeException {
        private final String reason;
        public OtpException(String reason, String message) {
            super(message);
            this.reason = reason;
        }
        public String reason() { return reason; }
    }

    /**
     * Inicia o envio do OTP para o destino.
     * @param canal   "email" ou "sms"
     * @param destino e-mail ou telefone (pode vir com máscara; a implementação normaliza)
     * @param previousDeliveryId (opcional) permite bypass do cooldown quando é reenvio do último deliveryId
     */
    StartResult start(String canal, String destino, String previousDeliveryId);

    /**
     * Verifica o código recebido e retorna um token de verificação (curta duração).
     */
    String verify(String deliveryId, String codeRaw);

    /**
     * Consome (invalida) o token, sem checar vínculo com destino.
     * Mantido para compatibilidade onde esse vínculo não é necessário.
     */
    boolean consumeToken(String token);

    /**
     * ✅ Novo: consome (invalida) o token garantindo que pertence ao destino informado.
     * Use este método para fluxos sensíveis como "esqueci minha senha".
     * A implementação deve normalizar o destino antes de comparar.
     */
    boolean consumeTokenForDestino(String token, String destino);
}
