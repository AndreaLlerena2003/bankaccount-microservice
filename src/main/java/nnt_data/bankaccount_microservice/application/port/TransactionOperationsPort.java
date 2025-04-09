package nnt_data.bankaccount_microservice.application.port;

import nnt_data.bankaccount_microservice.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionOperationsPort {
    Mono<Transaction> createTransaction(Transaction transaction);
    Flux<Transaction> getTransactions();
    Flux<Transaction> getTransactionsAccountId(String accountId);
}

/**
 * Interfaz TransactionOperationsPort
 * Define las operaciones relacionadas con transacciones bancarias utilizando un enfoque reactivo.
 * Las operaciones disponibles incluyen:
 * - Crear una nueva transacción.
 * - Obtener todas las transacciones registradas.
 * - Obtener transacciones asociadas a un ID de cuenta específica.
 *
 * Se emplean tipos reactivos (Mono y Flux) para garantizar un manejo asíncrono y eficiente de datos.
 */