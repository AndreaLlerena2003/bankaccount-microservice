package nnt_data.bankAccount_service.application.usecase.personal;

import nnt_data.bankAccount_service.application.usecase.AccountCreationStrategy;
import nnt_data.bankAccount_service.application.usecase.BaseAccountStrategy;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * PersonalAccountCreationStrategy es una implementación de la estrategia de creación de cuentas
 * personales. Esta clase extiende BaseAccountStrategy y proporciona validaciones específicas
 * para la creación de cuentas personales.
 *
 * Las cuentas personales deben tener un ID de cliente no nulo. Si la cuenta es de tipo FIXED_TERM,
 * se permite su creación sin validaciones adicionales. Para otros tipos de cuenta, se verifica que
 * el cliente no tenga ya una cuenta del mismo tipo.
 */
@Component
public class PersonalAccountCreationStrategy extends BaseAccountStrategy implements AccountCreationStrategy {

    private final BankAccountRepository accountRepository;

    public PersonalAccountCreationStrategy(ValidatorFactory validatorFactory,
                                           BankAccountRepository accountRepository) {
        super(validatorFactory);
        this.accountRepository = accountRepository;
    }

    @Override
    protected Mono<AccountBase> validateAccount(AccountBase account) {
        return Mono.defer(() -> {
            if (account.getCustomerId() == null) {
                return Mono.error(new IllegalArgumentException("El ID del cliente no puede ser null"));
            }
            if (account.getAccountType() == AccountType.FIXED_TERM) {
                return Mono.just(account);
            }
            return accountRepository.findByCustomerIdAndAccountType(account.getCustomerId(), account.getAccountType())
                    .hasElements()
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new IllegalArgumentException(
                                    "Cliente personal ya tiene una cuenta de tipo " + account.getAccountType()));
                        }
                        return Mono.just(account);
                    });
        });
    }

    @Override
    public Mono<AccountBase> createAccount(AccountBase account) {
        return validateAccount(account)
                .flatMap(this::validateWithFactory);
    }
}
