package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.NotaFiscalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/nota-fiscal")
@RequiredArgsConstructor
public class ConfirmaNotaFiscalApiController {

    private final NotaFiscalService service;

    @PostMapping("/confirmacao")
    public ResponseEntity<Void> confirmarNota(@Valid @RequestBody PreferenciaNotaDTO dto) {
        Long id = service.confirmar(dto.nome(), dto.preferencia(), dto.email());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        // LinkedHashMap para manter a ordem previsível
        Map<String, String> errors = new LinkedHashMap<>();

        // Erros de campo
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            String message = err.getDefaultMessage();
            errors.put(err.getField(), message != null ? message : "Campo inválido");
        }

        // Erros globais (sem campo específico), se existirem
        ex.getBindingResult().getGlobalErrors().forEach(objectError -> {
            String message = objectError.getDefaultMessage();
            errors.put(objectError.getObjectName(), message != null ? message : "Requisição inválida");
        });

        return ResponseEntity.badRequest().body(errors);
    }

    // DTO em record (Java 21) — imutável e compatível com Jackson/Spring Boot 3
    public record PreferenciaNotaDTO(
            @NotBlank(message = "Nome é obrigatório")
            String nome,

            @NotBlank(message = "Preferência é obrigatória")
            String preferencia,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail deve ser válido")
            String email
    ) { }
}
