/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.JsonMappingException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.annotation.Profile
 *  org.springframework.http.converter.HttpMessageNotReadableException
 *  org.springframework.web.bind.annotation.ControllerAdvice
 *  org.springframework.web.bind.annotation.ExceptionHandler
 */
package br.com.redemaisfarma.debug;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Profile(value={"dev", "docker"})
@ControllerAdvice
public class JsonDebugAdvice {
    private static final Logger log = LoggerFactory.getLogger(JsonDebugAdvice.class);

    @ExceptionHandler(value={HttpMessageNotReadableException.class})
    void onNotReadable(HttpMessageNotReadableException ex) {
        Object root;
        Object object = root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause() : ex;
        if (root instanceof JsonMappingException) {
            JsonMappingException jme = (JsonMappingException)root;
            log.error("JSON parse failed at {}: {}", new Object[]{jme.getPathReference(), ((Throwable)root).toString(), ex});
        } else {
            log.error("JSON parse failed: {}", (Object)((Throwable)root).toString(), (Object)ex);
        }
    }
}

