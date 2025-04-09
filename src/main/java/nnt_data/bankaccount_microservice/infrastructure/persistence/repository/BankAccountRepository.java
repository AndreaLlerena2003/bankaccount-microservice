package nnt_data.bankaccount_microservice.infrastructure.persistence.repository;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.model.AccountType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
/**
 * BankAccountRepository es una interfaz que extiende ReactiveMongoRepository y proporciona
 * métodos de acceso a datos para la entidad AccountBaseEntity. Define métodos adicionales
 * para verificar la existencia de cuentas y buscar cuentas por ID de cliente y tipo de cuenta.
 */
public interface BankAccountRepository extends ReactiveMongoRepository<AccountBaseEntity, String> {
    Mono<Boolean> existsByCustomerId(String customerId);
    Mono<Boolean> existsByAccountId(String accountId);
    Flux<AccountBaseEntity> findByCustomerIdAndAccountType(String customerId, AccountType accountType);
    Mono<Boolean> existsByCustomerIdAndAccountType(String customerId, AccountType accountType);
}
