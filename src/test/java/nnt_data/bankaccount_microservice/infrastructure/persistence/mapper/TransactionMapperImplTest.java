package nnt_data.bankaccount_microservice.infrastructure.persistence.mapper;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankaccount_microservice.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class TransactionMapperImplTest {

    @InjectMocks
    private TransactionMapperImpl transactionMapper;

    @Test
    void testToEntity() {
        // Arrange
        Transaction transaction = createSampleTransaction();

        // Act
        Mono<TransactionEntity> result = transactionMapper.toEntity(transaction);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertNotNull(entity);
                    assertEquals(transaction.getTransactionId(), entity.getTransactionId());
                    assertEquals(transaction.getDate(), entity.getDate());
                    assertEquals(transaction.getType(), entity.getType());
                    assertEquals(transaction.getAmount(), entity.getAmount());
                    assertEquals(transaction.getAccountId(), entity.getAccountId());
                })
                .verifyComplete();
    }

    @Test
    void testToDomain() {
        // Arrange
        TransactionEntity entity = createSampleTransactionEntity();

        // Act
        Mono<Transaction> result = transactionMapper.toDomain(entity);

        // Assert
        StepVerifier.create(result)
                .assertNext(domain -> {
                    assertNotNull(domain);
                    assertEquals(entity.getTransactionId(), domain.getTransactionId());
                    assertEquals(entity.getDate(), domain.getDate());
                    assertEquals(entity.getType(), domain.getType());
                    assertEquals(entity.getAmount(), domain.getAmount());
                    assertEquals(entity.getAccountId(), domain.getAccountId());
                })
                .verifyComplete();
    }

    private Transaction createSampleTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TRX123");
        transaction.setDate(new Date());
        transaction.setType(Transaction.TypeEnum.DEPOSIT);
        transaction.setAmount(new BigDecimal("500.75"));
        transaction.setAccountId("ACC456");
        return transaction;
    }

    private TransactionEntity createSampleTransactionEntity() {
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId("TRX789");
        entity.setDate(new Date());
        entity.setType(Transaction.TypeEnum.WITHDRAWAL);
        entity.setAmount(new BigDecimal("200.50"));
        entity.setAccountId("ACC987");
        return entity;
    }
}