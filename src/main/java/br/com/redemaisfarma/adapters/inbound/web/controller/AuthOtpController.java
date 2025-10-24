/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ProblemDetail
 *  org.springframework.http.ResponseEntity
 *  org.springframework.validation.annotation.Validated
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.dto.otp.OtpStartRequest;
import br.com.redemaisfarma.application.dto.otp.OtpStartResponse;
import br.com.redemaisfarma.application.dto.otp.OtpVerifyRequest;
import br.com.redemaisfarma.application.dto.otp.OtpVerifyResponse;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth/otp"}, produces={"application/json"})
@Validated
public class AuthOtpController {
    private static final Logger log = LoggerFactory.getLogger(AuthOtpController.class);
    private final OtpServicePort otp;

    public AuthOtpController(OtpServicePort otp) {
        this.otp = otp;
    }

    @PostMapping(value={"/start"}, consumes={"application/json"})
    public ResponseEntity<?> start(@Valid @RequestBody OtpStartRequest req) {
        try {
            OtpServicePort.StartResult res = this.otp.start(req.canal(), req.destino(), req.previousDeliveryId());
            return ResponseEntity.ok((Object)new OtpStartResponse(res.deliveryId(), res.maskedDestino(), res.cooldownSec(), res.ttlSeconds(), res.demoCode()));
        }
        catch (OtpServicePort.OtpException ex) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail((HttpStatusCode)HttpStatus.BAD_REQUEST, (String)ex.getMessage());
            pd.setTitle("OTP n\u00e3o enviado");
            pd.setProperty("reason", (Object)ex.reason());
            return ResponseEntity.badRequest().body((Object)pd);
        }
        catch (Exception ex) {
            log.error("Falha inesperada em /otp/start", (Throwable)ex);
            ProblemDetail pd = ProblemDetail.forStatusAndDetail((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR, (String)"N\u00e3o foi poss\u00edvel enviar o c\u00f3digo agora. Tente novamente.");
            pd.setTitle("Erro interno");
            pd.setProperty("reason", (Object)"internal_error");
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body((Object)pd);
        }
    }

    @PostMapping(value={"/verify"}, consumes={"application/json"})
    public ResponseEntity<?> verify(@Valid @RequestBody OtpVerifyRequest req) {
        try {
            String token = this.otp.verify(req.deliveryId(), req.code());
            return ResponseEntity.ok((Object)new OtpVerifyResponse(token));
        }
        catch (OtpServicePort.OtpException ex) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail((HttpStatusCode)HttpStatus.BAD_REQUEST, (String)ex.getMessage());
            pd.setTitle("OTP inv\u00e1lido");
            pd.setProperty("reason", (Object)ex.reason());
            return ResponseEntity.status((HttpStatusCode)HttpStatus.BAD_REQUEST).body((Object)pd);
        }
        catch (Exception ex) {
            log.error("Falha inesperada na verifica\u00e7\u00e3o de OTP", (Throwable)ex);
            ProblemDetail pd = ProblemDetail.forStatusAndDetail((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR, (String)"N\u00e3o foi poss\u00edvel verificar o c\u00f3digo agora. Tente novamente.");
            pd.setTitle("Erro interno");
            pd.setProperty("reason", (Object)"internal_error");
            return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body((Object)pd);
        }
    }
}

