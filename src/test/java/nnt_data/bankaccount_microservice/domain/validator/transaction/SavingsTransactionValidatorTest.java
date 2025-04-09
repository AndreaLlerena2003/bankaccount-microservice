package nnt_data.bankaccount_microservice.domain.validator.transaction;

import nnt_data.bankaccount_microservice.domain.validator.TransactionContext;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankaccount_microservice.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SavingsTransactionValidatorTest {

    @InjectMocks
    private SavingsTransactionValidator validator;

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionContext context;
    private Transaction transaction;
    private AccountBaseEntity account;

    @BeforeEach
    void setup() {
        account = new AccountBaseEntity();
        account.setBalance(BigDecimal.valueOf(1000));
        account.setMonthlyMovementLimit(3);


        transaction = new Transaction();
        transaction.setAccountId("12345");
        transaction.setType(Transaction.TypeEnum.WITHDRAWAL);
        transaction.setDate(new Date());

        context = new TransactionContext(account, transaction);
    }

    @Test
    void validate_whenWithdrawalWithZeroBalance_shouldReturnError() {
        when(transactionRepository.findAll()).thenReturn(Flux.empty());
        account.setBalance(BigDecimal.ZERO);
        StepVerifier.create(validator.validate(context))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("No se puede realizar retiros con saldo cero"))
                .verify();
    }


    @Test
    void validate_whenBalanceIsPositiveAndNoTransactions_shouldPassValidation() {
        when(transactionRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(validator.validate(context))
                .expectNextMatches(validatedContext -> validatedContext.getTransaction().getType() == Transaction.TypeEnum.WITHDRAWAL &&
                        validatedContext.getAccount().getBalance().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();
    }

}