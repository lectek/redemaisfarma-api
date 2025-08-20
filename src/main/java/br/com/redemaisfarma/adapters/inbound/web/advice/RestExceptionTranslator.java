package br.com.redemaisfarma.adapters.inbound.web.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionTranslator {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    var errors = ex.getBindingResult().getFieldErrors()
        .stream().collect(java.util.stream.Collectors.toMap(
            fe -> fe.getField(), fe -> fe.getDefaultMessage(), (a,b) -> a));
    return ResponseEntity.badRequest().body(Map.of(
        "status", 400, "error", "Bad Request", "validationErrors", errors));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("status", 500, "error", "Internal Server Error"));
  }
}
