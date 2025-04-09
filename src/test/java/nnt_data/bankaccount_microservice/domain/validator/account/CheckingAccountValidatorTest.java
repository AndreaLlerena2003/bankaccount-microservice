package nnt_data.bankaccount_microservice.domain.validator.account;

import nnt_data.bankaccount_microservice.model.AccountBase;
import nnt_data.bankaccount_microservice.model.AccountType;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class CheckingAccountValidatorTest {

    private CheckingAccountValidator validator;
    private AccountBase account;

    @BeforeEach
    void setup() {

        account = new AccountBase();
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setMovementLimit(1);
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setFeePerTransaction(BigDecimal.valueOf(5.00));
        account.setCustomerId("34567");
        account.setMaintenanceFee(BigDecimal.ZERO);

        validator = new CheckingAccountValidator();
    }

    @Test
    void validate_whenMaintenanceFeeIsNegative_shouldReturnError() {
        account.setMaintenanceFee(BigDecimal.valueOf(-10.00));

        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("La comisión de mantenimiento no puede ser negativa"))
                .verify();
    }

    @Test
    void validate_whenMonthlyMovementLimitIsNotNull_shouldReturnError() {
        account.setMonthlyMovementLimit(10);
        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Este tipo de cuenta no tiene limite de movimientos para mantenimiento"))
                .verify();
    }

    @Test
    void validate_whenAccountIsPymeAndFeeIsPositive_shouldReturnError() {
        account.setCustomerSubType(CustomerSubtype.PYME);
        account.setMaintenanceFee(BigDecimal.valueOf(50.00));

        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Las cuentas corrientes de clientes PYME no deben tener comision de mantenimiento"))
                .verify();
    }

    @Test
    void validate_whenAccountIsPymeAndNoCreditCard_shouldReturnError() {
        account.setCustomerSubType(CustomerSubtype.PYME);
        account.setMaintenanceFee(BigDecimal.ZERO);
        CheckingAccountValidator validatorWithMockedCreditCard = new CheckingAccountValidator() {
            @Override
            public Mono<Boolean> hasCreditCard(String customerId) {
                return Mono.just(false);
            }
        };

        StepVerifier.create(validatorWithMockedCreditCard.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Las Pymes no pueden abrir cuenta sin tarjeta de credito"))
                .verify();
    }

    @Test
    void validate_whenAccountIsPymeAndHasCreditCardAndValidFee_shouldPassValidation() {
        account.setCustomerSubType(CustomerSubtype.PYME);
        account.setMaintenanceFee(BigDecimal.ZERO);

        CheckingAccountValidator validatorWithMockedCreditCard = new CheckingAccountValidator() {
            @Override
            public Mono<Boolean> hasCreditCard(String customerId) {
                return Mono.just(true);
            }
        };

        StepVerifier.create(validatorWithMockedCreditCard.validate(account))
                .expectNextMatches(validatedAccount -> validatedAccount.getCustomerSubType().equals(CustomerSubtype.PYME) &&
                        validatedAccount.getMaintenanceFee().compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }

    @Test
    void validate_whenAccountIsNotPymeAndValid_shouldPassValidation() {
        account.setCustomerSubType(CustomerSubtype.VIP);
        account.setMaintenanceFee(BigDecimal.valueOf(20.00));
        account.setMonthlyMovementLimit(null);

        StepVerifier.create(validator.validate(account))
                .expectNextMatches(validatedAccount -> validatedAccount.getCustomerSubType().equals(CustomerSubtype.VIP) &&
                        validatedAccount.getMaintenanceFee().compareTo(BigDecimal.valueOf(20.00)) == 0 &&
                        validatedAccount.getMonthlyMovementLimit() == null)
                .verifyComplete();
    }
}