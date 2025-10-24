/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.RestControllerAdvice
 */
package br.com.redemaisfarma.adapters.inbound.web.advice;

import br.com.redemaisfarma.adapters.inbound.web.dto.ProblemDetails;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionTranslator {
    @ExceptionHandler(value={IllegalArgumentException.class})
    public ResponseEntity<ProblemDetails> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetails body = new ProblemDetails();
        body.setStatus(HttpStatus.BAD_REQUEST.value());
        body.setTitle("Requisi\u00e7\u00e3o inv\u00e1lida");
        body.setDetail(ex.getMessage());
        body.setTimestamp(Instant.now());
        return ResponseEntity.badRequest().body((Object)body);
    }

    @ExceptionHandler(value={Exception.class})
    public ResponseEntity<ProblemDetails> handleGeneric(Exception ex) {
        ProblemDetails body = new ProblemDetails();
        body.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.setTitle("Erro interno");
        body.setDetail(ex.getMessage());
        body.setTimestamp(Instant.now());
        return ResponseEntity.status((HttpStatusCode)HttpStatus.INTERNAL_SERVER_ERROR).body((Object)body);
    }
}

