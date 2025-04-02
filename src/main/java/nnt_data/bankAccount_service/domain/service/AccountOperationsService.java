package nnt_data.bankAccount_service.domain.service;

import nnt_data.bankAccount_service.application.port.AccountOperationsPort;
import nnt_data.bankAccount_service.application.usecase.AccountUpdateStrategy;
import nnt_data.bankAccount_service.infrastructure.persistence.mapper.AccountMapper;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.CustomerType;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.application.usecase.AccountCreationStrategy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * AccountOperationsService es un servicio que implementa AccountOperationsPort y proporciona
 * operaciones para la gestión de cuentas bancarias. Utiliza estrategias de creación y actualización
 * de cuentas basadas en el tipo de cliente, así como un repositorio para la persistencia de datos.
 */
@Service
public class AccountOperationsService implements AccountOperationsPort {

    private final Map<CustomerType, AccountCreationStrategy> creationStrategies;
    private final BankAccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final Map<CustomerType, AccountUpdateStrategy> updateStrategies;
    private static final Logger log = LoggerFactory.getLogger(AccountOperationsService.class);

    public AccountOperationsService(Map<CustomerType, AccountCreationStrategy> creationStrategies,
                                    BankAccountRepository accountRepository,
                                    Map<CustomerType, AccountUpdateStrategy> updateStrategies,
                                    AccountMapper accountMapper) {
        this.creationStrategies = creationStrategies;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.updateStrategies = updateStrategies;
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
        return executeUpdateStrategy(accountId,updatedAccount)
                .flatMap(this::saveAccount);
    }

    private Mono<AccountBase> executeCreationStrategy(AccountBase account) {
        log.info("Iniciando executeCreationStrategy para cuenta tipo: {}, clienteID: {}",
                account.getAccountType(), account.getCustomerId());
        return Mono.just(account)
                .filter(acc -> acc.getCustomerType() != null)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Error: El tipo de cliente es null");
                    return Mono.error(new IllegalArgumentException("El tipo de cliente no puede ser null"));
                }))
                .flatMap(acc -> {
                    AccountCreationStrategy strategy = creationStrategies.get(acc.getCustomerType());
                    if (strategy == null) {
                        log.error("Estrategia no encontrada para tipo de cliente: {}", acc.getCustomerType());
                        return Mono.error(new IllegalArgumentException(
                                "Tipo de cliente no soportado: " + acc.getCustomerType()));
                    }
                    log.debug("Usando estrategia: {} para cliente tipo: {}",
                            strategy.getClass().getSimpleName(), acc.getCustomerType());
                    return strategy.createAccount(acc)
                            .doOnNext(createdAccount ->
                                    log.info("Cuenta creada exitosamente: ID={}, tipo={}",
                                            createdAccount.getAccountId(), createdAccount.getAccountType()))
                            .doOnError(error ->
                                    log.error("Error al crear cuenta: {} - {}",
                                            error.getClass().getSimpleName(), error.getMessage()))
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("La estrategia devolvió un flujo vacío sin generar cuenta");
                                return Mono.error(new RuntimeException(
                                        "No se pudo crear la cuenta (flujo vacío para customerType=" +
                                                acc.getCustomerType() + ")"));
                            }));
                })
                .doOnError(error ->
                        log.error("Error final en executeCreationStrategy: {} - {}",
                                error.getClass().getSimpleName(), error.getMessage()));
    }

    private Mono<AccountBase> executeUpdateStrategy(String accountId,AccountBase updatedAccount) {
        return Mono.just(updatedAccount)
                .filter(acc -> acc.getCustomerType() != null)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El tipo de cliente no puede ser null")))
                .flatMap(acc -> Mono.justOrEmpty(updateStrategies.get(acc.getCustomerType()))
                        .switchIfEmpty(Mono.error(
                                new IllegalArgumentException("Tipo de cliente no soportado: " + acc.getCustomerType())))
                        .flatMap(strategy -> strategy.updateAccount(accountId, acc)));
    }

    private Mono<AccountBase> saveAccount(AccountBase account) {
        return Mono.just(account)
                .flatMap(accountMapper::toEntity)
                .flatMap(accountRepository::save)
                .flatMap(accountMapper::toDomain);
    }

}
