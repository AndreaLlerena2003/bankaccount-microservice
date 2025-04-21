package nnt_data.bankaccount_microservice.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.api.AccountsApi;
import nnt_data.bankaccount_microservice.application.port.AccountOperationsPort;
import nnt_data.bankaccount_microservice.application.port.TransactionOperationsPort;
import nnt_data.bankaccount_microservice.domain.service.ReportingService;
import nnt_data.bankaccount_microservice.model.AccountBase;
import nnt_data.bankaccount_microservice.model.PostSalarySummaryForPeriodRequest;
import nnt_data.bankaccount_microservice.model.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BankAccountController es un controlador REST que implementa AccountsApi y proporciona
 * endpoints para la gestión de cuentas bancarias y transacciones. Utiliza puertos de operaciones
 * para interactuar con los servicios de cuentas y transacciones.
 */
@RestController
@RequiredArgsConstructor
public class BankAccountController implements AccountsApi {

    private final AccountOperationsPort accountOperationsPort;
    private final ReportingService reportingService;
    private final TransactionOperationsPort transactionOperationsPort;
    private static final Logger log = LoggerFactory.getLogger(BankAccountController.class);

    @Override
    public Mono<ResponseEntity<Map<String, Object>>> createAccount(Mono<AccountBase> accountBase, ServerWebExchange exchange) {
        log.info("Procesando solicitud para crear nueva cuenta bancaria");
        return accountBase
                .flatMap(accountOperationsPort::createAccount)
                .map(account -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Cuenta creada exitosamente");
                    response.put("account_id", account.getAccountId());
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(response);
                });
    }

    /**
     * POST /accounts/transactions : Registrar una nueva transacción en la cuenta
     *
     * @param transaction (required)
     * @param exchange
     * @return Transacción registrada exitosamente (status code 201)
     * or Solicitud incorrecta (status code 400)
     * or Recurso no encontrado (status code 404)
     * or Error de validación (status code 422)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> createTransaction(Mono<Transaction> transaction, ServerWebExchange exchange) {
        log.info("Iniciando registro de nueva transacción");
        return transaction
                .flatMap(tx -> transactionOperationsPort.createTransaction(tx))
                .map(createdTransaction -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Transacción creada exitosamente");
                    response.put("transaction_id", createdTransaction.getTransactionId());
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(response);
                });
    }


    @Override
    public Mono<ResponseEntity<Map<String, Object>>> deleteAccount(String accountId, ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cuenta eliminada exitosamente");
        response.put("accountId", accountId);
        log.info("Iniciando eliminación de cuenta con ID: {}", accountId);
        return accountOperationsPort.deleteAccount(accountId)
                .then(Mono.just(ResponseEntity.ok().body(response)));
    }

    @Override
    public Mono<ResponseEntity<Map<String, Object>>> getAccountById(String accountId, ServerWebExchange exchange) {
        log.info("Buscando cuenta por ID: {}", accountId);
        return accountOperationsPort.findAccount(accountId)
                .map(account -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("accounts", account);
                    return ResponseEntity.ok()
                            .body(response);
                });
    }

    @Override
    public Mono<ResponseEntity<Map<String, Object>>> getAllAccounts(ServerWebExchange exchange) {
        log.info("Obteniendo listado de todas las cuentas bancarias");
        return accountOperationsPort.findAllAccounts()
                .collectList()
                .map(accounts -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("accounts", accounts);
                    return ResponseEntity.ok()
                            .body(response);
                });
    }

    /**
     * GET /accounts/transactions : Obtener todas las transacciones
     *
     * @param exchange
     * @return Historial completo de transacciones (status code 200)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> getAllTransactions(ServerWebExchange exchange) {
        log.info("Obteniendo listado de todas las trasnaciones");
        return transactionOperationsPort.getTransactions()
                .collectList()
                .map(transactions -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("transactions", transactions);
                    return ResponseEntity.ok()
                            .body(response);
                });
    }

    /**
     * GET /accounts/reporting/commissionReport : Obtener el reporte de comisiones
     *
     * @param startDate Fecha de inicio del reporte (required)
     * @param endDate   Fecha de fin del reporte (required)
     * @param accountId ID de la cuenta bancaria (required)
     * @param exchange
     * @return Reporte de comisiones generado exitosamente (status code 200)
     * or Solicitud incorrecta (status code 400)
     * or Recurso no encontrado (status code 404)
     * or Error de validación (status code 422)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> getCommissionReport(Date startDate, Date endDate, String accountId, ServerWebExchange exchange) {
        return reportingService.generateCommissionReportByAccountId(accountId, startDate, endDate)
                .map(report -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("report", report);
                    return ResponseEntity.ok()
                            .body(response);
                })
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }



    /**
     * GET /accounts/{accountId}/transactions : Obtener transacciones por ID de cuenta
     *
     * @param accountId ID de la cuenta bancaria (required)
     * @param exchange
     * @return Transacciones de la cuenta especificada (status code 200)
     * or Recurso no encontrado (status code 404)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> getTransactionsByAccountId(String accountId, ServerWebExchange exchange) {
        log.info("Trayendo transacciones by Account Id");
        return transactionOperationsPort.getTransactionsAccountId(accountId)
                .collectList()
                .map(transactions -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("transactions", transactions);
                    return ResponseEntity.ok()
                            .body(response);
                });
    }

    /**
     * POST /accounts/reporting/salarySummaryForPeriod : reporte
     *
     * @param postSalarySummaryForPeriodRequest (required)
     * @param exchange
     * @return Resumen del usuario especificado (status code 200)
     * or Solicitud incorrecta (status code 400)
     * or Recurso no encontrado (status code 404)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> postSalarySummaryForPeriod(Mono<PostSalarySummaryForPeriodRequest> postSalarySummaryForPeriodRequest, ServerWebExchange exchange) {
        return postSalarySummaryForPeriodRequest
                .flatMap(request -> reportingService.generateResumeOfAvarageBalanceForPeriodByAccountId(request.getAccountId(), request.getStartDate(), request.getEndDate())
                        .map(creditResumes -> {
                            Map<String, Object> response = new HashMap<>();
                            response.put("accountId", request.getAccountId());
                            response.put("BankAccountResumes", creditResumes);
                            return ResponseEntity.ok(response);
                        })
                        .onErrorResume(e -> {
                            Map<String, Object> errorResponse = new HashMap<>();
                            errorResponse.put("error", e.getMessage());
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                        }));
    }
    /**
     * GET /accounts/reporting/salarySummary/{customerId} : Obtener el reporte de salarios promedios para un cliente
     *
     * @param customerId ID del usuario (required)
     * @param exchange
     * @return Resumen del usuario especificado (status code 200)
     * or Recurso no encontrado (status code 404)
     * or Error interno del servidor (status code 500)
     */
    @Override
    public Mono<ResponseEntity<Map<String, Object>>> getSummarySalaryByCustomerId(String customerId, ServerWebExchange exchange) {
        return reportingService.generateResumeOfAvarageBalance(customerId)
                .collectList()
                .map(resumes -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("resume", resumes);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @Override
    public Mono<ResponseEntity<Map<String, Object>>> updateAccount(String accountId, Mono<AccountBase> accountBase, ServerWebExchange exchange) {
        log.info("Update Account By accountId");
        return accountBase
                .flatMap(account -> accountOperationsPort.updateAccount(accountId, account))
                .map(account -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Cuenta actualizada exitosamente");
                    response.put("account_id", accountId);
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(response);
                });
    }



}
