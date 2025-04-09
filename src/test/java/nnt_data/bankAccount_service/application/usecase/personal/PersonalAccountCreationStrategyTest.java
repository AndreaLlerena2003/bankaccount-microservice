package nnt_data.bankAccount_service.application.usecase.personal;

import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import nnt_data.bankAccount_service.model.CustomerSubtype;
import nnt_data.bankAccount_service.model.CustomerType;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalAccountCreationStrategyTest {

    @Mock
    private ValidatorFactory validatorFactory;

    @Mock
    private BankAccountRepository accountRepository;

    @InjectMocks
    private PersonalAccountCreationStrategy personalAccountCreationStrategy;

    private AccountBase validAccount;

    @BeforeEach
    void setUp() {
        validAccount = new AccountBase();
        validAccount.setAccountId("123");
        validAccount.setCustomerId("123456789");
        validAccount.setAccountType(AccountType.SAVINGS);
        validAccount.setCustomerType(CustomerType.PERSONAL);
        validAccount.setCustomerSubType(CustomerSubtype.REGULAR);
        validAccount.setMovementLimit(4);
        validAccount.setFeePerTransaction(BigDecimal.valueOf(5.00));
        validAccount.setMovementLimit(6);
        validAccount.setBalance(BigDecimal.valueOf(1000.00));
    }

    @Test
    void createAccount_whenCustomerSubTypeIsNull_shouldReturnError() {
        validAccount.setCustomerSubType(null);
        StepVerifier.create(personalAccountCreationStrategy.createAccount(validAccount))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("El subtipo de cuenta no puede ser null"))
                .verify();
    }

    @Test
    void createAccount_whenAccountTypeIsFixedTerm_shouldReturnSuccess() {
        validAccount.setAccountType(AccountType.FIXED_TERM);
        when(validatorFactory.getAccountValidator(AccountType.FIXED_TERM))
                .thenReturn(Mono::just);
        StepVerifier.create(personalAccountCreationStrategy.createAccount(validAccount))
                .expectNextMatches(account -> account.getAccountType() == AccountType.FIXED_TERM)
                .verifyComplete();
    }

    @Test
    void createAccount_whenAccountIsValid_shouldReturnSuccess() {
        when(accountRepository.findByCustomerIdAndAccountType(eq(validAccount.getCustomerId()), eq(validAccount.getAccountType())))
                .thenReturn(Flux.empty());
        when(validatorFactory.getAccountValidator(validAccount.getAccountType()))
                .thenReturn(Mono::just);
        StepVerifier.create(personalAccountCreationStrategy.createAccount(validAccount))
                .expectNextMatches(account -> account.getCustomerId().equals(validAccount.getCustomerId()) &&
                        account.getAccountType() == AccountType.SAVINGS)
                .verifyComplete();
    }

    @Test
    void createAccount_whenCustomerIdIsNull_shouldReturnError() {
        validAccount.setCustomerId(null);
        StepVerifier.create(personalAccountCreationStrategy.createAccount(validAccount))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("El ID del cliente no puede ser null"))
                .verify();
    }



}
