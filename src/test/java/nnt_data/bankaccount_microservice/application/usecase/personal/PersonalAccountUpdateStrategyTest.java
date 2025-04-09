package nnt_data.bankaccount_microservice.application.usecase.personal;

import nnt_data.bankaccount_microservice.domain.validator.factory.ValidatorFactory;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankaccount_microservice.model.AccountBase;
import nnt_data.bankaccount_microservice.model.AccountType;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalAccountUpdateStrategyTest {
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private BankAccountRepository accountRepository;
    @InjectMocks
    private PersonalAccountUpdateStrategy personalAccountUpdateStrategy;

    private AccountBase existingAccount;
    private AccountBase updateAccountRequest;

    @BeforeEach
    void setUp() {
        existingAccount = new AccountBase();
        existingAccount.setAccountId("123");
        existingAccount.setCustomerId("123456789");
        existingAccount.setAccountType(AccountType.SAVINGS);
        existingAccount.setCustomerType(CustomerType.PERSONAL);
        existingAccount.setCustomerSubType(CustomerSubtype.REGULAR);
        existingAccount.setBalance(BigDecimal.valueOf(2000.00));

        updateAccountRequest = new AccountBase();
        updateAccountRequest.setCustomerId("123456789");
        updateAccountRequest.setAccountType(AccountType.SAVINGS);
        updateAccountRequest.setCustomerType(CustomerType.PERSONAL);
        updateAccountRequest.setCustomerSubType(CustomerSubtype.REGULAR);
        updateAccountRequest.setBalance(BigDecimal.valueOf(3000.00));
    }

    @Test
    void updateAccount_whenCustomerIdIsNull_shouldReturnError() {
        updateAccountRequest.setCustomerId(null);
        StepVerifier.create(personalAccountUpdateStrategy.updateAccount("123", updateAccountRequest))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("El ID del cliente no puede ser null"))
                .verify();
    }


    @Test
    void updateAccount_whenAccountDoesNotExist_shouldReturnError() {
        // Simular repositorio devolviendo vacío (cuenta inexistente)
        when(accountRepository.findById("123")).thenReturn(Mono.empty());

        // Ejecutar estrategia y validar
        StepVerifier.create(personalAccountUpdateStrategy.updateAccount("123", updateAccountRequest))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Account not found with ID: 123"))
                .verify();
    }


    @Test
    void updateAccount_whenUpdateIsValid_shouldReturnUpdatedAccount() {
        AccountBaseEntity existingAccount = new AccountBaseEntity();
        existingAccount.setAccountId("123");
        existingAccount.setCustomerId("123456789");
        existingAccount.setAccountType(AccountType.SAVINGS);
        existingAccount.setCustomerType(CustomerType.PERSONAL);
        existingAccount.setCustomerSubType(CustomerSubtype.REGULAR);
        existingAccount.setBalance(BigDecimal.valueOf(2000.00));

        updateAccountRequest.setAccountId("123");
        updateAccountRequest.setCustomerId("123456789");
        updateAccountRequest.setAccountType(AccountType.SAVINGS);
        updateAccountRequest.setCustomerType(CustomerType.PERSONAL);
        updateAccountRequest.setCustomerSubType(CustomerSubtype.VIP);
        updateAccountRequest.setBalance(BigDecimal.valueOf(3000.00));

        when(accountRepository.findById("123")).thenReturn(Mono.just(existingAccount));
        when(validatorFactory.getAccountValidator(AccountType.SAVINGS))
                .thenReturn(Mono::just);

        StepVerifier.create(personalAccountUpdateStrategy.updateAccount("123", updateAccountRequest))
                .expectNextMatches(updatedAccount ->
                        updatedAccount.getAccountId().equals("123") &&
                                updatedAccount.getCustomerId().equals("123456789") &&
                                updatedAccount.getAccountType() == AccountType.SAVINGS &&
                                updatedAccount.getCustomerSubType() == CustomerSubtype.VIP &&
                                updatedAccount.getBalance().compareTo(BigDecimal.valueOf(3000.00)) == 0
                )
                .verifyComplete();
    }

}
