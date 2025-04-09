package nnt_data.bankaccount_microservice.domain.validator;


import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import reactor.core.publisher.Mono;

/**
 * TransactionValidator es una interfaz que extiende Validator y proporciona
 * validaciones específicas para contextos de transacciones.
 */
public interface TransactionValidator extends Validator<TransactionContext> {
    default Mono<Boolean> hasExceededLimit(AccountBaseEntity account) {
        if (account.getTransactionMovements() >= account.getMovementLimit()) {
            return Mono.error(new IllegalArgumentException("El número de transacciones ha superado el límite permitido"));
        } else {
            return Mono.just(true);
        }
    }
}
