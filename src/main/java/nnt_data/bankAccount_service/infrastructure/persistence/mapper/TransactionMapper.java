package nnt_data.bankAccount_service.infrastructure.persistence.mapper;

import nnt_data.bankAccount_service.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankAccount_service.model.Transaction;
import reactor.core.publisher.Mono;
/**
 * TransactionMapper es una interfaz que define métodos para mapear entre entidades de transacción
 * y objetos de dominio. Utiliza Reactor Mono para manejar el mapeo de manera asíncrona.
 */
public interface TransactionMapper {
    Mono<TransactionEntity> toEntity(Transaction transaction);
    Mono<Transaction> toDomain(TransactionEntity transactionEntity);
}
