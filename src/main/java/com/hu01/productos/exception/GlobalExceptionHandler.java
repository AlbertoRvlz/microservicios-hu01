package com.hu01.productos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(
            RecursoNoEncontradoException exception, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<ApiError> manejarDatoDuplicado(
            DatoDuplicadoException exception, HttpServletRequest request) {
        return crearRespuesta(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    private ResponseEntity<ApiError> crearRespuesta(HttpStatus status, String mensaje, HttpServletRequest request) {
        ApiError body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), mensaje,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
