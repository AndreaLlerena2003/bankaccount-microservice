package nnt_data.bankaccount_microservice.domain.validator.account;

import nnt_data.bankaccount_microservice.model.AccountBase;
import nnt_data.bankaccount_microservice.model.AccountType;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;


@ExtendWith(MockitoExtension.class)
public class FixedTermAccountValidatorTest {

    @InjectMocks
    private FixedTermAccountValidator validator; // Inyectar dependencias simuladas (si fueran necesarias)

    private AccountBase account;

    @BeforeEach
    void setup() {
        account = new AccountBase();
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setCustomerType(CustomerType.BUSINESS);
        account.setMovementLimit(1);
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setFeePerTransaction(BigDecimal.valueOf(5.00));
        account.setCustomerId("34567");
        account.setAccountType(AccountType.FIXED_TERM);
        account.setAllowedDayOfMonth("14");
    }

    @Test
    void validate_whenAllowedDayOfMonthIsNull_shouldReturnError() {
        account.setAllowedDayOfMonth(null);
        StepVerifier.create(validator.validate(account))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Debe especificar el dia de retiro permitido"))
                .verify();
    }

    @Test
    void validate_whenAllowedDayOfMonthIsValid_shouldPassValidation() {
        account.setAllowedDayOfMonth("10");
        StepVerifier.create(validator.validate(account))
                .expectNextMatches(validatedAccount -> validatedAccount.getAllowedDayOfMonth().equals("10") &&
                        validatedAccount.getAccountType() == AccountType.FIXED_TERM)
                .verifyComplete();
    }

    @Test
    void validate_whenAllowedDayOfMonthIsMaximumValue_shouldPassValidation() {
        account.setAllowedDayOfMonth("31");
        StepVerifier.create(validator.validate(account))
                .expectNextMatches(validatedAccount -> validatedAccount.getAllowedDayOfMonth().equals("31") &&
                        validatedAccount.getAccountType() == AccountType.FIXED_TERM)
                .verifyComplete();
    }

}
