package nnt_data.bankaccount_microservice.domain.validator.transaction;


import nnt_data.bankaccount_microservice.domain.validator.TransactionContext;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import nnt_data.bankaccount_microservice.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

public class CheckingTransactionValidatorTest {

    private CheckingTransactionValidator validator;
    private TransactionContext context;

    @BeforeEach
    void setup() {
        validator = new CheckingTransactionValidator();
        AccountBaseEntity account = new AccountBaseEntity();
        account.setCustomerId("34567");
        account.setCustomerSubType(CustomerSubtype.REGULAR);
        account.setCustomerType(CustomerType.PERSONAL);
        account.setBalance(BigDecimal.valueOf(1000.00));

        Transaction transaction = new Transaction();
        transaction.setType(Transaction.TypeEnum.DEPOSIT);

        context = new TransactionContext(account,transaction);

    }

    @Test
    void validate_whenTransactionIsWithdrawalAndBalanceIsZero_shouldReturnError() {
        context.getAccount().setBalance(BigDecimal.ZERO);
        context.getTransaction().setType(Transaction.TypeEnum.WITHDRAWAL);

        StepVerifier.create(validator.validate(context))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("No se puede realizar retiros con saldo cero"))
                .verify();
    }

    @Test
    void validate_whenTransactionIsWithdrawalAndBalanceIsNegative_shouldReturnError() {
        context.getAccount().setBalance(BigDecimal.valueOf(-100.00));
        context.getTransaction().setType(Transaction.TypeEnum.WITHDRAWAL);

        StepVerifier.create(validator.validate(context))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("No se puede realizar retiros con saldo cero"))
                .verify();
    }

    @Test
    void validate_whenTransactionIsWithdrawalAndBalanceIsPositive_shouldPassValidation() {
        context.getAccount().setBalance(BigDecimal.valueOf(1000.00));
        context.getTransaction().setType(Transaction.TypeEnum.WITHDRAWAL);

        StepVerifier.create(validator.validate(context))
                .expectNextMatches(validatedContext -> validatedContext.getAccount().getBalance().compareTo(BigDecimal.valueOf(1000.00)) == 0 &&
                        validatedContext.getTransaction().getType() == Transaction.TypeEnum.WITHDRAWAL)
                .verifyComplete();
    }

    @Test
    void validate_whenTransactionIsNotWithdrawal_shouldPassValidation() {
        context.getTransaction().setType(Transaction.TypeEnum.DEPOSIT);

        StepVerifier.create(validator.validate(context))
                .expectNextMatches(validatedContext -> validatedContext.getTransaction().getType() == Transaction.TypeEnum.DEPOSIT)
                .verifyComplete();
    }
}
