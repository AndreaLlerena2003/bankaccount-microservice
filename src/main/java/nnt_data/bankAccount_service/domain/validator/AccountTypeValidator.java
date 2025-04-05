package nnt_data.bankAccount_service.domain.validator;

import nnt_data.bankAccount_service.model.AccountBase;
import reactor.core.publisher.Mono;

/**
 * AccountTypeValidator es una interfaz que extiende Validator y proporciona
 * validaciones específicas para tipos de cuentas bancarias.
 */
public interface AccountTypeValidator extends Validator<AccountBase> {

    default Mono<Boolean> hasCreditCard(String customerId){
        return Mono.just(true); // validacion si cliente pyme tiene credit card
    }

    default Mono<Boolean> hasValidLimitsAndFees(AccountBase account){
        if(account.getMovementLimit() == null || account.getFeePerTransaction() == null){
            return Mono.error(new IllegalArgumentException("Los limites y comisiones no pueden ser nulos"));
        }
        if(account.getTransactionMovements() == null){ //implica una nueva creacion
            account.setTransactionMovements(0);
        }
        return Mono.just(true);
    }
}
