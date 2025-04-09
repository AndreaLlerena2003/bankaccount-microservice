package nnt_data.bankaccount_microservice.domain.validator.transaction;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.domain.validator.TransactionContext;
import nnt_data.bankaccount_microservice.domain.validator.TransactionValidator;
import nnt_data.bankaccount_microservice.model.Transaction;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CheckingTransactionValidator implements TransactionValidator {

    @Override
    public Mono<TransactionContext> validate(TransactionContext entity) {
        return validateWithdrawalBalance(entity)
                .then(Mono.just(entity));
    }

    private Mono<Void> validateWithdrawalBalance(TransactionContext context) {
        if (context.getTransaction().getType() == Transaction.TypeEnum.WITHDRAWAL &&
                context.getAccount().getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("No se puede realizar retiros con saldo cero"));
        }
        return Mono.empty();
    }

}
