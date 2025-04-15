package nnt_data.bankaccount_microservice.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.api.DebitCardApi;
import nnt_data.bankaccount_microservice.domain.service.DebitCardOperationsService;
import nnt_data.bankaccount_microservice.domain.service.ReportingService;
import nnt_data.bankaccount_microservice.model.AssocieteAccountToCardRequest;
import nnt_data.bankaccount_microservice.model.CreateTransactionFromDebitCardRequest;
import nnt_data.bankaccount_microservice.model.DebitCard;
import nnt_data.bankaccount_microservice.model.TransactionReport;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DebitCardController implements DebitCardApi {

    private final DebitCardOperationsService debitCardOperationsService;
    private final ReportingService reportingService;

    /**
     * PUT /debitCard/associeteAccountToCard : Asociar cuenta a tarjeta de debito
     *
     * @param associeteAccountToCardRequest (required)
     * @param exchange
     * @return Tarjeta actualizada exitosamente (status code 200)
     * or Solicitud incorrecta (status code 400)
     * or Recurso no encontrado (status code 404)
     * or Error de validación (status code 422)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> associeteAccountToCard(Mono<AssocieteAccountToCardRequest> associeteAccountToCardRequest, ServerWebExchange exchange) {
        return associeteAccountToCardRequest
                .flatMap(request -> {
                    if (request.getCardId() == null || request.getAccountId() == null) {
                        return Mono.error(new IllegalArgumentException("El ID de la tarjeta y el ID de la cuenta son obligatorios"));
                    }
                    return debitCardOperationsService.associateAccountToCard(request.getCardId(), request.getAccountId());
                })
                .map(updatedCard -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("debitCard", updatedCard);
                    response.put("message", "Cuenta asociada exitosamente a la tarjeta de débito");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                })
                .onErrorResume(e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    if (e instanceof IllegalArgumentException) {
                        errorResponse.put("error", e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
                    }
                    errorResponse.put("error", "Error al asociar la cuenta a la tarjeta: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }

    /**
     * POST /debitCard/create : Crear tarjeta de debito
     *
     * @param debitCard (required)
     * @param exchange
     * @return Tarjeat de debito creada exitosamente (status code 201)
     * or Solicitud incorrecta (status code 400)
     * or Error de validación (status code 422)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> createDebitCard(Mono<DebitCard> debitCard, ServerWebExchange exchange) {
        return debitCard
                .flatMap(debitCardOperationsService::createDebitCard)
                .map(savedCard -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("debitCard", savedCard);
                    response.put("message", "Tarjeta de débito creada con éxito");
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorResume(e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    if (e instanceof IllegalArgumentException) {
                        errorResponse.put("error", e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
                    }
                    errorResponse.put("error", "Error al crear la tarjeta de débito: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }

    /**
     * POST /debitCard/createTransaction : Crear transacción desde cuenta de débito
     *
     * @param createTransactionFromDebitCardRequest (required)
     * @param exchange
     * @return Transacción creada exitosamente (status code 200)
     * or Solicitud incorrecta (status code 400)
     * or Recurso no encontrado (status code 404)
     * or Error de validación (status code 422)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> createTransactionFromDebitCard(Mono<CreateTransactionFromDebitCardRequest> createTransactionFromDebitCardRequest, ServerWebExchange exchange) {
        return createTransactionFromDebitCardRequest
                .flatMap(request -> {
                    if (request.getCardNumber() == null || request.getTransaction() == null) {
                        return Mono.error(new IllegalArgumentException("El número de tarjeta y los datos de la transacción son obligatorios"));
                    }

                    return debitCardOperationsService.processDebitCardTransaction(request.getCardNumber(), request.getTransaction());
                })
                .map(processedTransaction -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("transaction", processedTransaction);
                    response.put("message", "Transacción procesada exitosamente");
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorResume(e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    if (e instanceof IllegalArgumentException) {
                        errorResponse.put("error", e.getMessage());
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
                    }
                    errorResponse.put("error", "Error al procesar la transacción: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }

    /**
     * GET /debitCard/{cardNumber}/transactions : Obtiene los últimos 10 movimientos de una tarjeta de débito
     * Devuelve un reporte con las últimas 10 transacciones realizadas con la tarjeta de débito especificada
     *
     * @param cardNumber Número de la tarjeta de débito (required)
     * @param exchange
     * @return Reporte de transacciones generado exitosamente (status code 200)
     * or Número de tarjeta inválido o no encontrado (status code 400)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<TransactionReport>> getLastTenTransactions(String cardNumber, ServerWebExchange exchange) {
        return reportingService.getLastTenTransactions(cardNumber)
                .map(transactionReport -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(transactionReport))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .build()))
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build()));
    }

    /**
     * GET /debitCard/{cardNumber}/getPrimaryAccountBalance : Obtiene el balance de la cuenta principal asociada
     * Devuelve el balance de la cuenta principal asociada a la tarjeta de débito especificada
     *
     * @param cardNumber Número de la tarjeta de débito (required)
     * @param exchange
     * @return Reporte de transacciones generado exitosamente (status code 200)
     * or Número de tarjeta inválido o no encontrado (status code 400)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> getPrimaryAccountBalance(String cardNumber, ServerWebExchange exchange) {
        return debitCardOperationsService.getPrimaryAccountBalance(cardNumber)
                .map(balance -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("balance", balance);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(response);
                });
    }

}
