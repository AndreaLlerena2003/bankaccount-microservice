package nnt_data.bankAccount_service.application.usecase;

import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.model.AccountBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * PersonalAccountUpdateStrategy es una implementación de la estrategia de actualización de cuentas
 * personales. Esta clase extiende BaseAccountStrategy y proporciona validaciones específicas
 * para la actualización de cuentas personales.
 *
 * Las cuentas personales deben tener un ID de cliente no nulo. No se permite cambiar el tipo de cuenta
 * durante la actualización. Utiliza una fábrica de validadores para realizar validaciones adicionales
 * y un repositorio para verificar la existencia de la cuenta.
 */
@Component
public class PersonalAccountUpdateStrategy extends BaseAccountStrategy implements AccountUpdateStrategy {
    private final BankAccountRepository accountRepository;

    public PersonalAccountUpdateStrategy(ValidatorFactory validatorFactory,
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
            return accountRepository.findById(account.getAccountId())
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found with ID: " + account.getAccountId())))
                    .flatMap(existingAccount -> {
                        if (!existingAccount.getAccountType().equals(account.getAccountType())) {
                            return Mono.error(new IllegalArgumentException("No se permite cambiar el tipo de cuenta"));
                        }
                        return Mono.just(account);
                    });
        });
    }

    @Override
    public Mono<AccountBase> updateAccount(String accountId, AccountBase account) {
        account.setAccountId(accountId);
        return validateAccount(account)
                .flatMap(this::validateWithFactory)
                .flatMap(validatedAccount -> accountRepository.findById(accountId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found with ID: " + accountId)))
                        .map(existingAccount -> {
                            validatedAccount.setAccountId(accountId);
                            return validatedAccount;
                        }));
    }
}