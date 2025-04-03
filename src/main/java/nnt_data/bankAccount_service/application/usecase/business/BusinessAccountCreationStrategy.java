package nnt_data.bankAccount_service.application.usecase.business;

import nnt_data.bankAccount_service.application.usecase.AccountCreationStrategy;
import nnt_data.bankAccount_service.application.usecase.BaseAccountStrategy;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * BusinessAccountCreationStrategy es una implementación de la estrategia de creación de cuentas
 * empresariales. Esta clase extiende BaseAccountStrategy y proporciona validaciones específicas
 * para cuentas empresariales.
 * Las cuentas empresariales solo pueden ser de tipo CHECKING y deben tener al menos un propietario
 * con identificación válida. Utiliza una fábrica de validadores para realizar validaciones adicionales.
 */

@Component
public class BusinessAccountCreationStrategy extends BaseAccountStrategy implements AccountCreationStrategy {

    public BusinessAccountCreationStrategy(ValidatorFactory validatorFactory) {
        super(validatorFactory);
    }

    @Override
    protected Mono<AccountBase> validateAccount(AccountBase account) {
        return Mono.defer(() -> validateCustomerSubtype(account.getCustomerSubType(), account.getCustomerType())
                .flatMap(validSubtype -> {
                    if (account.getAccountType() != AccountType.CHECKING) {
                        return Mono.error(new IllegalArgumentException(
                                "Cuentas empresariales solo pueden ser de tipo CHECKING"));
                    }
                    if (account.getOwners() == null || account.getOwners().isEmpty()) {
                        return Mono.error(new IllegalArgumentException(
                                "Cuenta empresarial debe tener un propietario con identificación válida"));
                    }
                    return Mono.just(account);
                }));
    }

    @Override
    public Mono<AccountBase> createAccount(AccountBase account) {
        return validateAccount(account)
                .flatMap(this::validateWithFactory);
    }
}