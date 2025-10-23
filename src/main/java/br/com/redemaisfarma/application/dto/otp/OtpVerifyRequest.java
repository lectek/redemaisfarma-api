package br.com.redemaisfarma.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OtpVerifyRequest(

    @NotBlank(message = "deliveryId é obrigatório")
    @Size(max = 128, message = "deliveryId muito longo")
    @JsonAlias({"delivery_id","deliveryId"})   // <- aceita snake e camel
    String deliveryId,

    @NotBlank(message = "code é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "code deve ter 6 dígitos")
    String code
) {}
