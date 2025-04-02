package nnt_data.bankAccount_service.application.usecase;

import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.model.AccountBase;
import reactor.core.publisher.Mono;

/**
 * BaseAccountStrategy es una clase abstracta que proporciona una estrategia base
 * para la validación de cuentas bancarias. Utiliza una fábrica de validadores
 * para obtener el validador adecuado según el tipo de cuenta.
 */

@RequiredArgsConstructor
public abstract  class BaseAccountStrategy {

    protected final ValidatorFactory validatorFactory;
    protected Mono<AccountBase> validateWithFactory(AccountBase account) {
        return validatorFactory.getAccountValidator(account.getAccountType())
                .validate(account);
    }
    protected abstract Mono<AccountBase> validateAccount(AccountBase account);
}
