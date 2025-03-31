package nnt_data.bankAccount_service.infrastructure.persistence.repository;

import nnt_data.bankAccount_service.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<TransactionEntity, String> {

    Flux<TransactionEntity> findByAccountId(String accountId);
}
