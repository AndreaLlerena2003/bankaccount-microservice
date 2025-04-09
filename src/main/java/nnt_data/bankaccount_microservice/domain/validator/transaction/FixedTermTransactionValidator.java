package nnt_data.bankaccount_microservice.domain.validator.transaction;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.domain.validator.TransactionContext;
import nnt_data.bankaccount_microservice.domain.validator.TransactionValidator;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankaccount_microservice.model.Transaction;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class FixedTermTransactionValidator implements TransactionValidator {

    private final TransactionRepository transactionRepository;
    @Override
    public Mono<TransactionContext> validate(TransactionContext entity) {
        return validateWithdrawalBalance(entity)
                .then(Mono.defer(() -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(entity.getTransaction().getDate());
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                    if (dayOfMonth != Integer.parseInt(entity.getAccount().getAllowedDayOfMonth())) {
                        return Mono.error(new IllegalArgumentException(
                                "Solo se permiten transacciones el día " + entity.getAccount().getAllowedDayOfMonth()));
                    }
                    return transactionRepository.findAll()
                            .filter(t -> t.getAccountId().equals(entity.getTransaction().getAccountId()))
                            .filter(t -> isSameDay(t.getDate(), entity.getTransaction().getDate()))
                            .count()
                            .flatMap(count -> {
                                if (count > 0) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Solo se permite un movimiento por día"));
                                }
                                return Mono.just(entity);
                            });
                }));
    }

    private Mono<Void> validateWithdrawalBalance(TransactionContext context) {
        if (context.getTransaction().getType() == Transaction.TypeEnum.WITHDRAWAL &&
                context.getAccount().getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("No se puede realizar retiros con saldo cero"));
        }
        return Mono.empty();
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



}
