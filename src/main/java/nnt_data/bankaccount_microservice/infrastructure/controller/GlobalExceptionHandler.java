package nnt_data.bankaccount_microservice.infrastructure.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
/**
 * GlobalExceptionHandler es una clase de manejo global de excepciones para controladores REST.
 * Utiliza @RestControllerAdvice para interceptar y manejar excepciones específicas y genéricas,
 * proporcionando respuestas adecuadas con códigos de estado HTTP y mensajes de error.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String TIMESTAMP = "timestamp";

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleWebExchangeBindException(WebExchangeBindException ex) {
        Map<String, Object> response = new HashMap<>();
        return getValidationErrorResponse(response).apply(ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleServerWebInputException(ServerWebInputException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de solicitud");
        errorResponse.put("message", "Datos de entrada inválidos");

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    private Function<Throwable, Mono<ResponseEntity<Map<String, Object>>>> getValidationErrorResponse(
            Map<String, Object> response) {
        return t -> Mono.just(t).cast(WebExchangeBindException.class)
                .flatMap(e -> Mono.just(e.getFieldErrors()))
                .flatMapMany(Flux::fromIterable)
                .map(fieldError -> "Campo " + fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collectList()
                .flatMap(l -> {
                    response.put(TIMESTAMP, LocalDateTime.now());
                    response.put("code", "VALIDATION_ERROR");
                    response.put("status", HttpStatus.BAD_REQUEST.value());
                    response.put("error", "Validation Error");
                    response.put("details", l);
                    return Mono.just(ResponseEntity.badRequest().body(response));
                });
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Ha ocurrido un error interno. Por favor, intente más tarde.");

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }
}
