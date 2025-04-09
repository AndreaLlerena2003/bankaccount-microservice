package nnt_data.bankaccount_microservice.application.usecase.business;

import nnt_data.bankaccount_microservice.domain.validator.AccountTypeValidator;
import nnt_data.bankaccount_microservice.domain.validator.factory.ValidatorFactory;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankaccount_microservice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessAccountUpdateStrategyTest {
    @Mock
    private BankAccountRepository accountRepository;
    @Mock
    private ValidatorFactory validatorFactory;

    @InjectMocks
    private BusinessAccountUpdateStrategy strategy;

    private AccountBase account;

    @BeforeEach
    void setUp(){
        account = new AccountBase();
        account.setAccountId("123");
        account.setCustomerId("123456789");
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setMaintenanceFee(BigDecimal.valueOf(10.00));
        account.setMovementLimit(3);
        account.setFeePerTransaction(BigDecimal.valueOf(5.00));
        account.setOwners(List.of(new Person("12345678", "Luana Perez")));
        account.setBalance(BigDecimal.valueOf(4000.00));
    }

    @Test
    void updateAccount_shouldReturnAccountNotFoundError() {
        when(accountRepository.findById(anyString())).thenReturn(Mono.empty());
        StepVerifier.create(strategy.updateAccount("123", account))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        "Account not found with ID: 123".equals(error.getMessage()))
                .verify();
        verify(accountRepository, times(1)).findById("123");
    }

    @Test
    void updateAccount_shouldUpdateSuccessfully() {

        AccountBaseEntity accountBaseEntity = new AccountBaseEntity();
        accountBaseEntity.setAccountId("123");
        accountBaseEntity.setCustomerId("123456789");
        accountBaseEntity.setAccountType(AccountType.CHECKING);
        accountBaseEntity.setCustomerType(CustomerType.BUSINESS);
        accountBaseEntity.setCustomerSubType(CustomerSubtype.REGULAR);
        accountBaseEntity.setMaintenanceFee(BigDecimal.valueOf(10.00));
        accountBaseEntity.setMovementLimit(3);
        accountBaseEntity.setFeePerTransaction(BigDecimal.valueOf(5.00));
        accountBaseEntity.setOwners(List.of(new Person("12345678", "Luana Perez")));
        accountBaseEntity.setBalance(BigDecimal.valueOf(4000.00));

        when(accountRepository.findById("123")).thenReturn(Mono.just(accountBaseEntity));
        AccountTypeValidator mockValidator = mock(AccountTypeValidator.class);
        when(mockValidator.validate(any(AccountBase.class)))
                .thenReturn(Mono.just(account));
        when(validatorFactory.getAccountValidator(AccountType.CHECKING)).thenReturn(mockValidator);

        StepVerifier.create(strategy.updateAccount("123", account))
                .expectNextMatches(updatedAccount -> updatedAccount.getAccountId().equals("123")
                        && updatedAccount.getBalance().compareTo(BigDecimal.valueOf(4000.00)) == 0
                        && updatedAccount.getAccountType() == AccountType.CHECKING)
                .verifyComplete();
    }
}
