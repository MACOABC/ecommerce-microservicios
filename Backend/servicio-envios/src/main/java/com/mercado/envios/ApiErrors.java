package com.mercado.envios;

import java.time.Instant;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class ApiErrors {
    private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);
    private ResponseEntity<?> error(int status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now().toString(),
            "status", status, "message", message, "path", request.getRequestURI()));
    }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> business(ResponseStatusException e, HttpServletRequest r) {
        return error(e.getStatusCode().value(), e.getReason() == null ? "Solicitud inválida" : e.getReason(), r);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage()).sorted().reduce((a,b) -> a + "; " + b).orElse("Solicitud inválida");
        return error(400, message, r);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
        IllegalArgumentException.class})
    ResponseEntity<?> malformed(Exception e, HttpServletRequest r) { return error(400, "Formato de solicitud inválido", r); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflict(Exception e, HttpServletRequest r) { return error(409, "El registro está duplicado o tiene referencias activas", r); }
    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<?> remote(RestClientResponseException e, HttpServletRequest r) {
        int status = e.getStatusCode().is4xxClientError() ? e.getStatusCode().value() : 502;
        return error(status, status == 409 ? "Conflicto de stock o estado en el servicio remoto" :
            status == 404 ? "Recurso remoto no encontrado" : "No se pudo completar la operación remota; vuelve a intentarlo", r);
    }
    @ExceptionHandler(RestClientException.class)
    ResponseEntity<?> unavailable(Exception e, HttpServletRequest r) {
        return error(503, "Servicio remoto no disponible; reintenta la misma operación", r);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception e, HttpServletRequest r) {
        log.error("Error procesando {}", r.getRequestURI(), e);
        return error(500, "Error interno del servicio", r);
    }
}
