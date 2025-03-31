package nnt_data.bankAccount_service.infrastructure.persistence.mapper;


import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankAccount_service.model.Transaction;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TransactionMapperImpl implements TransactionMapper {

    public Mono<TransactionEntity> toEntity(Transaction transaction) {
        TransactionEntity transactionEntity = new TransactionEntity();
        BeanUtils.copyProperties(transaction, transactionEntity);
        return Mono.just(transactionEntity);
    }

    public Mono<Transaction> toDomain(TransactionEntity transactionEntity) {
        Transaction transaction = new Transaction();
        BeanUtils.copyProperties(transactionEntity, transaction);
        return Mono.just(transaction);
    }
}
