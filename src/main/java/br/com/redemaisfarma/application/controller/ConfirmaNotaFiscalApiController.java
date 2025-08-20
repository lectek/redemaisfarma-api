// src/main/java/br/com/redemaisfarma/adapters/inbound/web/controller/ConfirmaNotaFiscalApiController.java
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.NotaFiscalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/nota-fiscal")
public class ConfirmaNotaFiscalApiController {

    private final NotaFiscalService service;

    public ConfirmaNotaFiscalApiController(NotaFiscalService service) {
        this.service = service;
    }

    /** POST /nota-fiscal/confirmacao */
    @PostMapping("/confirmacao")
    public ResponseEntity<Void> confirmarNota(@Valid @RequestBody PreferenciaNotaDTO dto) {
        Long id = service.confirmar(dto.getNome(), dto.getPreferencia(), dto.getEmail());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build(); // 201 Created
    }

    /** 400 com mensagens de campo quando a validação falha */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @Data
    public static class PreferenciaNotaDTO {
        @NotBlank(message = "Nome é obrigatório")
        private String nome;

        @NotBlank(message = "Preferência é obrigatória")
        private String preferencia;

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ser válido")
        private String email;
    }
}
