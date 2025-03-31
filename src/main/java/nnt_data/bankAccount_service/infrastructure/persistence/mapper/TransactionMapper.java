package nnt_data.bankAccount_service.infrastructure.persistence.mapper;

import nnt_data.bankAccount_service.infrastructure.persistence.model.TransactionEntity;
import nnt_data.bankAccount_service.model.Transaction;
import reactor.core.publisher.Mono;

public interface TransactionMapper {
    Mono<TransactionEntity> toEntity(Transaction transaction);
    Mono<Transaction> toDomain(TransactionEntity transactionEntity);
}
