package nnt_data.bankAccount_service.infrastructure.persistence.repository;

import nnt_data.bankAccount_service.infrastructure.persistence.model.AccountBaseEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BankAccountRepository extends ReactiveMongoRepository<AccountBaseEntity, String> {
    Mono<Boolean> existsByCustomerId(String customerId);
    Mono<Boolean> existsByAccountId(String accountId);
}
