package nnt_data.bankAccount_service.domain.validator.account;


import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import nnt_data.bankAccount_service.model.CustomerSubtype;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;


@ExtendWith(MockitoExtension.class)
public class SavingsAccountValidatorTest {

    @InjectMocks
    private SavingsAccountValidator validator;

    private AccountBase account;

    @BeforeEach
    void setup() {
        account = new AccountBase();
        account.setAccountType(AccountType.SAVINGS);
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setMonthlyMovementLimit(5);
        account.setFeePerTransaction(BigDecimal.valueOf(10));
        account.setMovementLimit(10);
        account.setMinimumDailyAverage(null);
    }
    
    @Test
    void validate_whenMonthlyMovementLimitIsNull_shouldReturnError() {
        account.setMonthlyMovementLimit(null);

        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("El límite mensual de movimientos debe ser mayor a 0"))
                .verify();
    }

    @Test
    void validate_whenMonthlyMovementLimitIsLessThanOne_shouldReturnError() {
        account.setMonthlyMovementLimit(0);

        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("El límite mensual de movimientos debe ser mayor a 0"))
                .verify();
    }

    @Test
    void validate_whenMaintenanceFeeIsNotNull_shouldReturnError() {
        account.setMaintenanceFee(BigDecimal.valueOf(10.0));

        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Las cuentas de Ahorro no tienen comisiones"))
                .verify();
    }

    @Test
    void validate_whenAccountIsVipAndMinimumDailyAverageIsMissing_shouldReturnError() {
        account.setCustomerSubType(CustomerSubtype.VIP);
        account.setMinimumDailyAverage(null);

        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("La cuenta de ahorro debe especificar un monto mínimo de promedio diario"))
                .verify();
    }

    @Test
    void validate_whenAccountIsRegularAndValidParameters_shouldPassValidation() {
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setMonthlyMovementLimit(10);
        account.setMaintenanceFee(null);

        StepVerifier.create(validator.validate(account))
                .expectNextMatches(validatedAccount -> validatedAccount.getCustomerSubType() == CustomerSubtype.REGULAR &&
                        validatedAccount.getMonthlyMovementLimit() == 10 &&
                        validatedAccount.getMaintenanceFee() == null)
                .verifyComplete();
    }
}