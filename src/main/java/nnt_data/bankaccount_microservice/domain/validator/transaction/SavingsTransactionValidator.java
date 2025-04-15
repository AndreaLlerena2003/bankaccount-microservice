package nnt_data.bankaccount_microservice.domain.validator.transaction;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.domain.validator.TransactionContext;
import nnt_data.bankaccount_microservice.domain.validator.TransactionValidator;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.TransactionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class SavingsTransactionValidator implements TransactionValidator {

    private final TransactionRepository transactionRepository;

    private boolean isInCurrentMonth(Date date) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date());

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }

    @Override
    public Mono<TransactionContext> validate(TransactionContext entity) {
        return transactionRepository.findAll()
                        .filter(t -> t.getSourceAccountId().equals(entity.getTransaction().getSourceAccountId()))
                        .filter(t -> isInCurrentMonth(t.getDate()))
                        .count()
                        .flatMap(count -> {
                            if (count >= entity.getAccount().getMonthlyMovementLimit()) {
                                return Mono.error(new IllegalArgumentException(
                                        "Se ha excedido el límite de movimientos mensuales"));
                            }
                            return Mono.just(entity);
                        });
    }
}
