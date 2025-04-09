package nnt_data.bankAccount_service.domain.validator.transaction;

import nnt_data.bankAccount_service.domain.validator.TransactionContext;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankAccount_service.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FixedTermTransactionValidatorTest {

    @InjectMocks
    private FixedTermTransactionValidator validator;

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionContext context;
    private Transaction transaction;
    private AccountBaseEntity account;

    @BeforeEach
    void setup() {
        account = new AccountBaseEntity();
        account.setBalance(BigDecimal.valueOf(1000));
        account.setAllowedDayOfMonth("15");

        transaction = new Transaction();
        transaction.setAccountId("12345");
        transaction.setType(Transaction.TypeEnum.WITHDRAWAL);
        transaction.setDate(createDate(2023, 11, 15));
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setTransactionId("1325254");
        context = new TransactionContext(account,transaction);
    }

    @Test
    void validate_whenTransactionNotOnAllowedDay_shouldReturnError() {
        transaction.setDate(createDate(2023, 11, 14));
        StepVerifier.create(validator.validate(context))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Solo se permiten transacciones el día 15"))
                .verify();
    }

    @Test
    void validate_whenTransactionIsWithdrawalAndBalanceIsZero_shouldReturnError() {
        account.setBalance(BigDecimal.ZERO);
        StepVerifier.create(validator.validate(context))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("No se puede realizar retiros con saldo cero"))
                .verify();
    }

    @Test
    void validate_whenTransactionIsWithdrawalAndBalanceIsPositive_shouldPassValidation() {
        when(transactionRepository.findAll()).thenReturn(Flux.empty());
        StepVerifier.create(validator.validate(context))
                .expectNextMatches(validatedContext -> validatedContext.getTransaction().getType() == Transaction.TypeEnum.WITHDRAWAL &&
                        validatedContext.getAccount().getBalance().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();
    }

    @Test
    void validate_whenTransactionOnAllowedDayAndFirstTransaction_shouldPassValidation() {
        when(transactionRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(validator.validate(context))
                .expectNextMatches(validatedContext ->
                        isSameDay(validatedContext.getTransaction().getDate(), createDate(2023, 11, 15)) &&
                                validatedContext.getAccount().getAllowedDayOfMonth().equals("15"))
                .verifyComplete();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }
}