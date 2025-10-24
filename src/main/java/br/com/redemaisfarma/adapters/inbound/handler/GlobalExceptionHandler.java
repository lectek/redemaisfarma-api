/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.EntityNotFoundException
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.validation.ConstraintViolationException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.TypeMismatchException
 *  org.springframework.context.MessageSource
 *  org.springframework.context.i18n.LocaleContextHolder
 *  org.springframework.dao.DataIntegrityViolationException
 *  org.springframework.http.HttpHeaders
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.converter.HttpMessageNotReadableException
 *  org.springframework.lang.NonNull
 *  org.springframework.security.access.AccessDeniedException
 *  org.springframework.util.MultiValueMap
 *  org.springframework.validation.BindException
 *  org.springframework.validation.FieldError
 *  org.springframework.validation.ObjectError
 *  org.springframework.web.HttpMediaTypeNotSupportedException
 *  org.springframework.web.HttpRequestMethodNotSupportedException
 *  org.springframework.web.bind.MethodArgumentNotValidException
 *  org.springframework.web.bind.MissingServletRequestParameterException
 *  org.springframework.web.bind.annotation.ExceptionHandler
 *  org.springframework.web.bind.annotation.RestControllerAdvice
 *  org.springframework.web.context.request.ServletWebRequest
 *  org.springframework.web.context.request.WebRequest
 *  org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
 *  org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
 */
package br.com.redemaisfarma.adapters.inbound.handler;

import br.com.redemaisfarma.application.core.exception.InvalidCredentialsException;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler
extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "validation_failed", "Validation failed", request);
        HashMap<String, String> fields = new HashMap<String, String>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }
        StringBuilder global = new StringBuilder();
        for (ObjectError ge : ex.getBindingResult().getGlobalErrors()) {
            if (global.length() > 0) {
                global.append("; ");
            }
            global.append(ge.getDefaultMessage());
        }
        if (global.length() > 0) {
            body.put("globalError", global.toString());
        }
        body.put("validationErrors", fields);
        log.debug("Validation failed: {}", fields);
        return this.handleExceptionInternal((Exception)ex, body, headers, (HttpStatusCode)HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<Object> handleBindException(@NonNull BindException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "binding_failed", "Binding failed", request);
        HashMap fields = new HashMap();
        ex.getBindingResult().getFieldErrors().forEach(err -> fields.put(err.getField(), err.getDefaultMessage()));
        if (!ex.getBindingResult().getGlobalErrors().isEmpty()) {
            StringBuilder global = new StringBuilder();
            ex.getBindingResult().getGlobalErrors().forEach(ge -> {
                if (global.length() > 0) {
                    global.append("; ");
                }
                global.append(ge.getDefaultMessage());
            });
            body.put("globalError", global.toString());
        }
        body.put("validationErrors", fields);
        return this.handleExceptionInternal((Exception)ex, body, headers, (HttpStatusCode)HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value={ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "constraint_violation", "Constraint violation", req);
        HashMap fields = new HashMap();
        ex.getConstraintViolations().forEach(cv -> fields.put(cv.getPropertyPath().toString(), cv.getMessage()));
        body.put("validationErrors", fields);
        return ResponseEntity.badRequest().body(body);
    }

    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "malformed_json", "Malformed JSON request", request);
        log.debug("Malformed JSON", (Throwable)ex);
        return this.handleExceptionInternal((Exception)ex, body, headers, (HttpStatusCode)HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "missing_parameter", "Missing required parameter: " + ex.getParameterName(), request);
        return this.handleExceptionInternal((Exception)ex, body, headers, (HttpStatusCode)HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<Object> handleTypeMismatch(@NonNull TypeMismatchException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        String string;
        if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException matme = (MethodArgumentTypeMismatchException)ex;
            string = matme.getName();
        } else {
            string = ex.getPropertyName();
        }
        String paramName = string;
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "type_mismatch", "Invalid value for parameter: " + (paramName != null ? paramName : "unknown"), request);
        return this.handleExceptionInternal((Exception)ex, body, headers, (HttpStatusCode)HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull HttpRequestMethodNotSupportedException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Map<String, Object> body = this.baseBody(HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed", "Method not allowed: " + ex.getMethod(), request);
        return this.handleExceptionInternal((Exception)ex, body, headers, (HttpStatusCode)HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull HttpMediaTypeNotSupportedException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Map<String, Object> body = this.baseBody(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported_media_type", "Unsupported media type: " + String.valueOf(ex.getContentType()), request);
        return this.handleExceptionInternal((Exception)ex, body, headers, (HttpStatusCode)HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
    }

    @ExceptionHandler(value={InvalidCredentialsException.class})
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        Map<String, Object> body = this.baseBody(HttpStatus.UNAUTHORIZED, "invalid_credentials", ex.getMessage(), req);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(value={AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        Map<String, Object> body = this.baseBody(HttpStatus.FORBIDDEN, "access_denied", "Acesso negado.", req);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(value={EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<Object> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        Map<String, Object> body = this.baseBody(HttpStatus.NOT_FOUND, "not_found", ex.getMessage() != null ? ex.getMessage() : "Recurso n\u00e3o encontrado.", req);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(value={IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage(), req);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(value={DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String lower;
        String msg;
        String friendly = "Viola\u00e7\u00e3o de integridade de dados.";
        Throwable root = ex.getMostSpecificCause();
        String string = msg = root != null ? root.getMessage() : ex.getMessage();
        if (msg != null && ((lower = msg.toLowerCase()).contains("codigo_barras") || lower.contains("codigo barras") || lower.contains("unique") || lower.contains("uk_") || lower.contains("duplicate") || lower.contains("uniq"))) {
            friendly = "C\u00f3digo de barras j\u00e1 cadastrado para outro produto.";
        }
        Map<String, Object> body = this.baseBody(HttpStatus.CONFLICT, "data_integrity", friendly, req);
        log.warn("Data integrity violation: {}", (Object)msg);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(value={OtpServicePort.OtpException.class})
    public ResponseEntity<Object> handleOtp(OtpServicePort.OtpException ex, HttpServletRequest req) {
        Locale locale = LocaleContextHolder.getLocale();
        String key = "otp.error." + ex.reason();
        String i18n = this.resolveOrFallback(key, ex.getMessage(), locale);
        Map<String, Object> body = this.baseBody(HttpStatus.BAD_REQUEST, "otp_error", i18n, req);
        body.put("code", ex.reason());
        return ResponseEntity.badRequest().body(body);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf((int)statusCode.value());
        Map<String, Object> std = this.baseBody(status, "error", ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase(), request);
        if (body instanceof Map) {
            Map given = (Map)body;
            given.forEach((k, v) -> std.put(String.valueOf(k), v));
        } else if (body != null) {
            std.put("detail", body);
        }
        if (status.is5xxServerError()) {
            log.error("handleExceptionInternal (5xx)", (Throwable)ex);
        } else {
            log.debug("handleExceptionInternal (4xx): {}", (Object)ex.getMessage());
        }
        return new ResponseEntity(std, (MultiValueMap)headers, (HttpStatusCode)status);
    }

    private Map<String, Object> baseBody(HttpStatus status, String code, String message, WebRequest request) {
        ServletWebRequest swr;
        String path = null;
        if (request instanceof ServletWebRequest && (swr = (ServletWebRequest)request).getRequest() != null) {
            path = swr.getRequest().getRequestURI();
        }
        return this.baseBody(status, code, message, path);
    }

    private Map<String, Object> baseBody(HttpStatus status, String code, String message, HttpServletRequest req) {
        String path = req != null ? req.getRequestURI() : null;
        return this.baseBody(status, code, message, path);
    }

    private Map<String, Object> baseBody(HttpStatus status, String code, String message, String path) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("code", code);
        body.put("message", message);
        if (path != null) {
            body.put("path", path);
        }
        return body;
    }

    private String resolveOrFallback(String key, String fallback, Locale locale) {
        try {
            return this.messageSource.getMessage(key, null, locale);
        }
        catch (Exception e) {
            return fallback != null ? fallback : key;
        }
    }
}

