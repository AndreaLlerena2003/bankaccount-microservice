package nnt_data.bankAccount_service.application.usecase;

import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PersonalAccountCreationStrategy implements AccountCreationStrategy {

    private final ValidatorFactory validatorFactory;
    private final BankAccountRepository accountRepository;

    public PersonalAccountCreationStrategy(ValidatorFactory validatorFactory,
                                           BankAccountRepository accountRepository) {
        this.validatorFactory = validatorFactory;
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<AccountBase> createAccount(AccountBase account) {
        return Mono.defer(() -> validateExistingPersonalAccount(account)
                .flatMap(acc -> {
                    AccountType accountType = acc.getAccountType();
                    return validatorFactory.getAccountValidator(accountType)
                            .validate(account)
                            .thenReturn(acc);
                }));
    }

    private Mono<AccountBase> validateExistingPersonalAccount(AccountBase account) {
        return Mono.defer(() -> {
            if (account.getCustomerId() == null) {
                return Mono.error(new IllegalArgumentException("El ID del cliente no puede ser null"));
            }

            return accountRepository.existsByCustomerId(account.getCustomerId())
                    .flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new IllegalArgumentException(
                                    "Cliente personal ya tiene una cuenta creada"));
                        }
                        return Mono.just(account);
                    });
        });
    }
}