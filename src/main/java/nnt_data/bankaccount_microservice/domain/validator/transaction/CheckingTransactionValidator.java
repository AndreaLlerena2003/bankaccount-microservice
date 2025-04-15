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
        return Mono.just(entity);
    }


}
