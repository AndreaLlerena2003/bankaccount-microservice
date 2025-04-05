package nnt_data.bankAccount_service.application.usecase;

import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.CustomerSubtype;
import nnt_data.bankAccount_service.model.CustomerType;
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

    protected Mono<CustomerSubtype> validateCustomerSubtype(CustomerSubtype subtype, CustomerType customerType) {
        if (subtype == null) {
            return Mono.error(new IllegalArgumentException("El subtipo de cuenta no puede ser null"));
        }
        return switch (customerType) {
            case PERSONAL -> {
                if (!subtype.equals(CustomerSubtype.REGULAR) && !subtype.equals(CustomerSubtype.VIP)) {
                    yield Mono.error(new IllegalArgumentException(
                            "El subtipo de cuenta personal solo puede ser 'regular' o 'vip'"));
                }
                yield Mono.just(subtype);
            }
            case BUSINESS -> {
                if (!subtype.equals(CustomerSubtype.REGULAR) && !subtype.equals(CustomerSubtype.PYME)) {
                    yield Mono.error(new IllegalArgumentException(
                            "El subtipo de cuenta empresarial solo puede ser 'regular' o 'pyme'"));
                }
                yield Mono.just(subtype);
            }
        };
    }

}
