/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.http.ResponseEntity
 *  org.springframework.web.bind.MethodArgumentNotValidException
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.servlet.support.ServletUriComponentsBuilder
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.NotaFiscalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value={"/nota-fiscal"})
public class ConfirmaNotaFiscalApiController {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(ConfirmaNotaFiscalApiController.class);
    private final NotaFiscalService service;

    public ConfirmaNotaFiscalApiController(NotaFiscalService service) {
        this.service = service;
    }

    @PostMapping(value={"/confirmacao"})
    public ResponseEntity<Void> confirmarNota(@Valid @RequestBody PreferenciaNotaDTO dto) {
        Long id = this.service.confirmar(dto.getNome(), dto.getPreferencia(), dto.getEmail());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(new Object[]{id}).toUri();
        return ResponseEntity.created((URI)location).build();
    }

    @ExceptionHandler(value={MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        HashMap errors = new HashMap();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    public static class PreferenciaNotaDTO {
        @NotBlank(message="Nome \u00e9 obrigat\u00f3rio")
        private @NotBlank(message="Nome \u00e9 obrigat\u00f3rio") String nome;
        @NotBlank(message="Prefer\u00eancia \u00e9 obrigat\u00f3ria")
        private @NotBlank(message="Prefer\u00eancia \u00e9 obrigat\u00f3ria") String preferencia;
        @NotBlank(message="E-mail \u00e9 obrigat\u00f3rio")
        @Email(message="E-mail deve ser v\u00e1lido")
        private @NotBlank(message="E-mail \u00e9 obrigat\u00f3rio") @Email(message="E-mail deve ser v\u00e1lido") String email;

        @Generated
        public PreferenciaNotaDTO() {
        }

        @Generated
        public String getNome() {
            return this.nome;
        }

        @Generated
        public String getPreferencia() {
            return this.preferencia;
        }

        @Generated
        public String getEmail() {
            return this.email;
        }

        @Generated
        public void setNome(String nome) {
            this.nome = nome;
        }

        @Generated
        public void setPreferencia(String preferencia) {
            this.preferencia = preferencia;
        }

        @Generated
        public void setEmail(String email) {
            this.email = email;
        }

        @Generated
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof PreferenciaNotaDTO)) {
                return false;
            }
            PreferenciaNotaDTO other = (PreferenciaNotaDTO)o;
            if (!other.canEqual(this)) {
                return false;
            }
            String this$nome = this.getNome();
            String other$nome = other.getNome();
            if (this$nome == null ? other$nome != null : !this$nome.equals(other$nome)) {
                return false;
            }
            String this$preferencia = this.getPreferencia();
            String other$preferencia = other.getPreferencia();
            if (this$preferencia == null ? other$preferencia != null : !this$preferencia.equals(other$preferencia)) {
                return false;
            }
            String this$email = this.getEmail();
            String other$email = other.getEmail();
            return !(this$email == null ? other$email != null : !this$email.equals(other$email));
        }

        @Generated
        protected boolean canEqual(Object other) {
            return other instanceof PreferenciaNotaDTO;
        }

        @Generated
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            String $nome = this.getNome();
            result = result * 59 + ($nome == null ? 43 : $nome.hashCode());
            String $preferencia = this.getPreferencia();
            result = result * 59 + ($preferencia == null ? 43 : $preferencia.hashCode());
            String $email = this.getEmail();
            result = result * 59 + ($email == null ? 43 : $email.hashCode());
            return result;
        }

        @Generated
        public String toString() {
            return "ConfirmaNotaFiscalApiController.PreferenciaNotaDTO(nome=" + this.getNome() + ", preferencia=" + this.getPreferencia() + ", email=" + this.getEmail() + ")";
        }
    }
}

