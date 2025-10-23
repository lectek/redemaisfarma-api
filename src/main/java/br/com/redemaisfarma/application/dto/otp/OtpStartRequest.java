package br.com.redemaisfarma.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Pedido para iniciar o envio de um código OTP (e-mail ou SMS).
 *
 * Observações:
 * - canal: "email" ou "sms" (case-insensitive no serviço, aqui validado em minúsculas).
 * - destino: e-mail ou telefone; validação fina é feita no serviço.
 * - previousDeliveryId: opcional, use quando estiver reenviando (para burlar cooldown do último envio).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OtpStartRequest(

    /** Canal de entrega do OTP: "email" ou "sms". */
    @NotBlank(message = "canal é obrigatório")
    @Pattern(regexp = "email|sms", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "canal deve ser 'email' ou 'sms'")
    String canal,

    /** Destino do OTP: e-mail (canal=email) ou telefone (canal=sms). */
    @NotBlank(message = "destino é obrigatório")
    @Size(max = 256, message = "destino muito longo")
    String destino,

    /** (Opcional) ID de entrega anterior para controle de reenvio / cooldown. */
    @Size(max = 128, message = "previousDeliveryId muito longo")
    String previousDeliveryId
) {}
