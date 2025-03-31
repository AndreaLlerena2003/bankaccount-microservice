package nnt_data.bankAccount_service.infrastructure.service;

import nnt_data.bankAccount_service.domain.port.AccountOperationsPort;
import nnt_data.bankAccount_service.infrastructure.persistence.mapper.AccountMapper;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.CustomerType;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.application.usecase.AccountCreationStrategy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AccountOperationsService implements AccountOperationsPort {

    private final Map<CustomerType, AccountCreationStrategy> creationStrategies;
    private final BankAccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountOperationsService(Map<CustomerType, AccountCreationStrategy> creationStrategies,
                                    BankAccountRepository accountRepository,
                                    AccountMapper accountMapper) {
        this.creationStrategies = creationStrategies;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public Mono<AccountBase> findAccount(String accountId) {
        return accountRepository.findById(accountId)
                .doOnNext(accountFound -> System.out.println("Cuenta encontrada en BD: " + accountFound))
                .flatMap(accountMapper::toDomain)
                .doOnNext(mappedAccount -> System.out.println("Cuenta mapeada: " + mappedAccount))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No existe una cuenta con el ID: " + accountId)));
    }

    @Override
    public Mono<Void> deleteAccount(String accountId) {
        return accountRepository.existsById(accountId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException(
                                "No existe una cuenta con el ID: " + accountId));
                    }
                    return accountRepository.deleteById(accountId);
                });
    }

    @Override
    public Flux<AccountBase> findAllAccounts() {
        return accountRepository.findAll()
                .flatMap(accountMapper::toDomain);
    }

    @Override
    public Mono<AccountBase> createAccount(AccountBase accountBase) {
        return executeCreationStrategy(accountBase)
                .flatMap(this::saveAccount);
    }

    @Override
    public Mono<AccountBase> updateAccount(String accountId,AccountBase updatedAccount) {
        System.out.println("UPDATE BY ACCOUNT ID: " + accountId);
        return accountRepository.existsByAccountId(accountId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new IllegalArgumentException(
                                "No existe una cuenta con el ID: " + accountId));
                    }
                    updatedAccount.setAccountId(accountId);
                    return saveAccount(updatedAccount);
                });
    }

    private Mono<AccountBase> executeCreationStrategy(AccountBase account) {
        return Mono.just(account)
                .filter(acc -> acc.getCustomerType() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El tipo de cliente no puede ser null")))
                .flatMap(acc -> Mono.justOrEmpty(creationStrategies.get(acc.getCustomerType()))
                        .switchIfEmpty(Mono.error(
                                new IllegalArgumentException("Tipo de cliente no soportado: " + acc.getCustomerType())))
                        .flatMap(strategy -> strategy.createAccount(acc)));
    }

    private Mono<AccountBase> saveAccount(AccountBase account) {
        return Mono.just(account)
                .flatMap(accountMapper::toEntity)
                .flatMap(accountRepository::save)
                .flatMap(accountMapper::toDomain);
    }

}
