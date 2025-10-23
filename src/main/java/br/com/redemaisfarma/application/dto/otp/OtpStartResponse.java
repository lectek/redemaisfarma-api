package br.com.redemaisfarma.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Resposta do início do fluxo OTP.
 * Mantemos camelCase no contrato (alinhado ao controller).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OtpStartResponse(

    /** Identificador opaco desta tentativa/entrega. Envie depois no /verify. */
    String deliveryId,

    /** Destino mascarado para exibição ao usuário (ex.: "l****a@gmail.com"). */
    String maskedDestino,

    /** Tempo de espera, em segundos, para poder reenviar. */
    Integer cooldownSec,

    /** Tempo de vida do código, em segundos. */
    Integer ttlSeconds,

    /** (DEV/TEST) Código em claro (null em produção). */
    String demoCode
) {}
