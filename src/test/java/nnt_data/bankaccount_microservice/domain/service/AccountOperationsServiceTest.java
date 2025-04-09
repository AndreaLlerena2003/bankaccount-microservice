package nnt_data.bankaccount_microservice.domain.service;

import nnt_data.bankaccount_microservice.application.usecase.AccountCreationStrategy;
import nnt_data.bankaccount_microservice.application.usecase.AccountUpdateStrategy;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.mapper.AccountMapper;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankaccount_microservice.model.AccountBase;
import nnt_data.bankaccount_microservice.model.AccountType;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import nnt_data.bankaccount_microservice.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AccountOperationsServiceTest {

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountCreationStrategy personalCreationStrategy;

    @Mock
    private AccountCreationStrategy businessCreationStrategy;

    @Mock
    private AccountUpdateStrategy personalUpdateStrategy;

    @Mock
    private AccountUpdateStrategy businessUpdateStrategy;

    private Map<CustomerType, AccountCreationStrategy> creationStrategies;
    private Map<CustomerType, AccountUpdateStrategy> updateStrategies;

    @InjectMocks
    private AccountOperationsService accountOperationsService;

    private AccountBase testAccount;
    private AccountBaseEntity testAccountEntity;
    private final String ACCOUNT_ID = "acc123";

    @BeforeEach
    void setUp() {
        creationStrategies = new HashMap<>();
        creationStrategies.put(CustomerType.PERSONAL, personalCreationStrategy);
        creationStrategies.put(CustomerType.BUSINESS, businessCreationStrategy);

        updateStrategies = new HashMap<>();
        updateStrategies.put(CustomerType.PERSONAL, personalUpdateStrategy);
        updateStrategies.put(CustomerType.BUSINESS, businessUpdateStrategy);


        accountOperationsService = new AccountOperationsService(
                creationStrategies,
                accountRepository,
                updateStrategies,
                accountMapper
        );


        testAccount = new AccountBase();
        testAccount.setAccountId(ACCOUNT_ID);
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setCustomerId("cust123");
        testAccount.setCustomerType(CustomerType.PERSONAL);
        testAccount.setCustomerSubType(CustomerSubtype.REGULAR);
        testAccount.setOwners(new ArrayList<>());
        testAccount.setAuthorizedSigners(new ArrayList<>());

        testAccountEntity = new AccountBaseEntity();
        testAccountEntity.setAccountId(ACCOUNT_ID);
        testAccountEntity.setAccountType(AccountType.SAVINGS);
        testAccountEntity.setBalance(new BigDecimal("1000.00"));
        testAccountEntity.setCustomerId("cust123");
        testAccountEntity.setCustomerType(CustomerType.PERSONAL);
        testAccountEntity.setCustomerSubType(CustomerSubtype.REGULAR);
        testAccountEntity.setOwners(List.of(new Person()));
        testAccountEntity.setAuthorizedSigners(List.of(new Person()));
    }

    @Test
    void findAccountShouldReturnAccount() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Mono.just(testAccountEntity));
        when(accountMapper.toDomain(any(AccountBaseEntity.class))).thenReturn(Mono.just(testAccount));

        StepVerifier.create(accountOperationsService.findAccount(ACCOUNT_ID))
                .expectNext(testAccount)
                .verifyComplete();

        verify(accountRepository, times(1)).findById(ACCOUNT_ID);
        verify(accountMapper, times(1)).toDomain(any(AccountBaseEntity.class));
    }

    @Test
    void findAllAccountsShouldReturnAllAccounts() {
        List<AccountBaseEntity> accountEntities = List.of(testAccountEntity);
        when(accountRepository.findAll()).thenReturn(Flux.fromIterable(accountEntities));
        when(accountMapper.toDomain(any(AccountBaseEntity.class))).thenReturn(Mono.just(testAccount));


        StepVerifier.create(accountOperationsService.findAllAccounts())
                .expectNext(testAccount)
                .verifyComplete();

        verify(accountRepository, times(1)).findAll();
        verify(accountMapper, times(1)).toDomain(any(AccountBaseEntity.class));
    }


    @Test
    void createAccountShouldUseCorrectStrategy() {

        AccountBase expectedAccount = testAccount;
        AccountBaseEntity expectedEntity = testAccountEntity;

        when(personalCreationStrategy.createAccount(any())).thenReturn(Mono.just(expectedAccount));
        when(accountMapper.toEntity(any())).thenReturn(Mono.just(expectedEntity));
        when(accountRepository.save(any())).thenReturn(Mono.just(expectedEntity));
        when(accountMapper.toDomain(any())).thenReturn(Mono.just(expectedAccount));

        StepVerifier.create(accountOperationsService.createAccount(testAccount))
                .expectNext(expectedAccount)
                .verifyComplete();

        verify(personalCreationStrategy, times(1)).createAccount(any());
        verify(accountMapper, times(1)).toEntity(any());
        verify(accountRepository, times(1)).save(any());
        verify(accountMapper, times(1)).toDomain(any());
    }

    @Test
    void createAccountWithInvalidCustomerTypeShouldReturnError() {
        AccountBase invalidAccount = new AccountBase();
        invalidAccount.setCustomerType(null);
        StepVerifier.create(accountOperationsService.createAccount(invalidAccount))
                .expectErrorMatches(throwable ->
                        throwable.getMessage().contains("El tipo de cliente no puede ser null"))
                .verify();
    }
}