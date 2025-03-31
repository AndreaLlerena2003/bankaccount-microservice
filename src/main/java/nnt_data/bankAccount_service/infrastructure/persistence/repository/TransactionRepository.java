package nnt_data.bankAccount_service.infrastructure.persistence.repository;

import nnt_data.bankAccount_service.infrastructure.persistence.model.TransactionEntity;
import nnt_data.bankAccount_service.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveMongoRepository<TransactionEntity, String> {

    Flux<TransactionEntity> findByAccountId(String accountId);
}
