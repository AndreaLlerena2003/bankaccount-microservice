package nnt_data.bankAccount_service.application.usecase.business;

import nnt_data.bankAccount_service.application.usecase.AccountUpdateStrategy;
import nnt_data.bankAccount_service.application.usecase.BaseAccountStrategy;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * BusinessAccountUpdateStrategy es una implementación de la estrategia de actualización de cuentas
 * empresariales. Esta clase extiende BaseAccountStrategy y proporciona validaciones específicas
 * para la actualización de cuentas empresariales.*
 * Las cuentas empresariales solo pueden ser de tipo CHECKING y no se permite cambiar el tipo de cuenta.
 * Utiliza una fábrica de validadores para realizar validaciones adicionales y un repositorio para
 * verificar la existencia de la cuenta.
 */
@Component
public class BusinessAccountUpdateStrategy extends BaseAccountStrategy implements AccountUpdateStrategy {
    private final BankAccountRepository accountRepository;

    public BusinessAccountUpdateStrategy(ValidatorFactory validatorFactory,
                                         BankAccountRepository accountRepository) {
        super(validatorFactory);
        this.accountRepository = accountRepository;
    }

    @Override
    protected Mono<AccountBase> validateAccount(AccountBase account) {
        return Mono.defer(() -> validateCustomerSubtype(account.getCustomerSubType(),account.getCustomerType())
                .flatMap(validSubtype -> {
                    if(account.getAccountType() != AccountType.CHECKING){
                        return Mono.error(new IllegalArgumentException(
                                "Cuentas empresariales solo pueden ser de tipo CHECKING"));
                    }
                    return accountRepository.findById(account.getAccountId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Account not found with ID: " + account.getAccountId())))
                            .flatMap(existingAccount -> {
                                if (!existingAccount.getAccountType().equals(account.getAccountType())) {
                                    return Mono.error(new IllegalArgumentException("No se permite cambiar el tipo de cuenta"));
                                }
                                return Mono.just(account);
                            });
                }));
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