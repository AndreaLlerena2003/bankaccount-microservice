package nnt_data.bankAccount_service.application.usecase;

import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import nnt_data.bankAccount_service.model.Person;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class BusinessAccountCreationStrategy implements AccountCreationStrategy {
    private final ValidatorFactory validatorFactory;

    public BusinessAccountCreationStrategy(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    private Mono<AccountBase> validateBusinessAccount(AccountBase account) {
        return Mono.defer(() -> {
            if (account.getAccountType() != AccountType.CHECKING) {
                return Mono.error(new IllegalArgumentException(
                        "Cuentas empresariales solo pueden ser de tipo CHECKING"));
            }
            System.out.println(account);
            if (account.getOwners() == null || account.getOwners().isEmpty()) {
                System.out.println(account);
                return Mono.error(new IllegalArgumentException(
                        "Cuenta empresarial debe tener un propietario con identificación válida"));
            }
            if (account.getAuthorizedSignature() == null) {
                account.setAuthorizedSignature(Collections.singletonList(new Person()));
            }
            return Mono.just(account);
        });
    }

    @Override
    public Mono<AccountBase> createAccount(AccountBase account) {
        return validateBusinessAccount(account)
                .flatMap(acc -> {
                    AccountType accountType = acc.getAccountType();
                    return validatorFactory.getAccountValidator(accountType)
                            .validate(account)
                            .thenReturn(acc);
                });
    }
}