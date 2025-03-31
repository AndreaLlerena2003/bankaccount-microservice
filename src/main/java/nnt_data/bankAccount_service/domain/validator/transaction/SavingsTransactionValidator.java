package nnt_data.bankAccount_service.domain.validator.transaction;

import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.domain.validator.TransactionContext;
import nnt_data.bankAccount_service.domain.validator.TransactionValidator;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankAccount_service.model.Transaction;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class SavingsTransactionValidator implements TransactionValidator {

    private final TransactionRepository transactionRepository;

    private Mono<Void> validateWithdrawalBalance(TransactionContext context) {
        if (context.getTransaction().getType() == Transaction.TypeEnum.WITHDRAWAL &&
                context.getAccount().getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("No se puede realizar retiros con saldo cero"));
        }
        return Mono.empty();
    }

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
        return validateWithdrawalBalance(entity)
                .then(transactionRepository.findAll()
                        .filter(t -> t.getAccountId().equals(entity.getTransaction().getAccountId()))
                        .filter(t -> isInCurrentMonth(t.getDate()))
                        .count()
                        .flatMap(count -> {
                            if (count >= entity.getAccount().getMonthlyMovementLimit()) {
                                return Mono.error(new IllegalArgumentException(
                                        "Se ha excedido el límite de movimientos mensuales"));
                            }
                            return Mono.just(entity);
                        }));
    }
}
