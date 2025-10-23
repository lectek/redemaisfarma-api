package br.com.redemaisfarma.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

/**
 * Resposta bem-sucedida da verificação do OTP.
 * O token retornado deve ser enviado junto ao POST de criação de conta
 * para comprovar que o e-mail/telefone foi validado.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OtpVerifyResponse(

    /**
     * Token opaco de verificação para uso único (one-time).
     */
    @NotBlank(message = "verificationToken é obrigatório")
    String verificationToken
) {}
