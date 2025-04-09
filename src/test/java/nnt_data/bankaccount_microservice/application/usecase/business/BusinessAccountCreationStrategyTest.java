package nnt_data.bankaccount_microservice.application.usecase.business;

import nnt_data.bankaccount_microservice.domain.validator.AccountTypeValidator;
import nnt_data.bankaccount_microservice.domain.validator.factory.ValidatorFactory;
import nnt_data.bankaccount_microservice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BusinessAccountCreationStrategyTest {

    @Mock
    private ValidatorFactory validatorFactory;

    private BusinessAccountCreationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BusinessAccountCreationStrategy(validatorFactory);
        AccountTypeValidator accountValidator = mock(AccountTypeValidator.class);
        lenient().when(validatorFactory.getAccountValidator(any(AccountType.class))).thenReturn(accountValidator);
        lenient().when(accountValidator.validate(any(AccountBase.class))).thenReturn(Mono.just(new AccountBase()));
    }

    @Test
    @DisplayName("Debería crear cuenta empresarial válida")
    void shouldCreateValidBusinessAccount() {
        // Arrange
        AccountBase account = new AccountBase();
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setOwners(List.of(
               new Person("12345678", "Luana Perez")
        ));

        // Act & Assert
        StepVerifier.create(strategy.createAccount(account))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería rechazar cuenta empresarial que no es tipo CHECKING")
    void shouldRejectNonCheckingBusinessAccount() {
        // Arrange
        AccountBase account = new AccountBase();
        account.setAccountType(AccountType.SAVINGS);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setOwners(List.of(
                new Person("12345678", "Luana Perez")
        ));

        // Act & Assert
        StepVerifier.create(strategy.createAccount(account))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("solo pueden ser de tipo CHECKING"))
                .verify();
    }

    @Test
    @DisplayName("Debería rechazar cuenta empresarial sin propietarios")
    void shouldRejectBusinessAccountWithoutOwners() {
        // Arrange
        AccountBase account = new AccountBase();
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setOwners(Collections.emptyList());

        // Act & Assert
        StepVerifier.create(strategy.createAccount(account))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("debe tener un propietario"))
                .verify();
    }

    @Test
    @DisplayName("Debería rechazar cuenta empresarial con subtipo inválido")
    void shouldRejectBusinessAccountWithInvalidSubtype() {
        // Arrange
        AccountBase account = new AccountBase();
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setCustomerSubType(CustomerSubtype.VIP); // Subtipo inválido para empresarial
        account.setOwners(List.of(
                new Person("12345678", "Luana Perez")
        ));

        // Act & Assert
        StepVerifier.create(strategy.createAccount(account))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("solo puede ser 'regular' o 'pyme'"))
                .verify();
    }

    @Test
    @DisplayName("Debería rechazar cuenta empresarial con subtipo nulo")
    void shouldRejectBusinessAccountWithNullSubtype() {
        // Arrange
        AccountBase account = new AccountBase();
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setCustomerSubType(null);
        account.setOwners(List.of(
                new Person("12345678", "Luana Perez")
        ));

        // Act & Assert
        StepVerifier.create(strategy.createAccount(account))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().contains("no puede ser null"))
                .verify();
    }
}