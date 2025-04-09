package nnt_data.bankAccount_service.infrastructure.persistence.entity;

import nnt_data.bankAccount_service.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityTest {

    @Test
    void testTransactionEntityGettersAndSetters() {
        // Arrange
        TransactionEntity transaction = new TransactionEntity();
        String transactionId = "TRX123";
        Date date = new Date();
        Transaction.TypeEnum type = Transaction.TypeEnum.DEPOSIT; // Asumiendo que TypeEnum tiene valores como DEPOSIT
        BigDecimal amount = new BigDecimal("500.75");
        String accountId = "ACC456";

        // Act
        transaction.setTransactionId(transactionId);
        transaction.setDate(date);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setAccountId(accountId);

        // Assert
        assertEquals(transactionId, transaction.getTransactionId());
        assertEquals(date, transaction.getDate());
        assertEquals(type, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(accountId, transaction.getAccountId());
    }

    @Test
    void testTransactionEntityEqualsAndHashCode() {
        // Arrange
        TransactionEntity transaction1 = new TransactionEntity();
        transaction1.setTransactionId("TRX123");
        transaction1.setAmount(new BigDecimal("500.75"));
        transaction1.setAccountId("ACC456");

        TransactionEntity transaction2 = new TransactionEntity();
        transaction2.setTransactionId("TRX123");
        transaction2.setAmount(new BigDecimal("500.75"));
        transaction2.setAccountId("ACC456");

        TransactionEntity transaction3 = new TransactionEntity();
        transaction3.setTransactionId("TRX789");
        transaction3.setAmount(new BigDecimal("200.50"));
        transaction3.setAccountId("ACC987");

        // Assert
        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
        assertNotEquals(transaction1, transaction3);
        assertNotEquals(transaction1.hashCode(), transaction3.hashCode());
    }

    @Test
    void testTransactionEntityToString() {
        // Arrange
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransactionId("TRX123");
        transaction.setAmount(new BigDecimal("500.75"));
        transaction.setAccountId("ACC456");

        // Act
        String toString = transaction.toString();

        // Assert
        assertTrue(toString.contains("TRX123"));
        assertTrue(toString.contains("500.75"));
        assertTrue(toString.contains("ACC456"));
    }

    @Test
    void testCreateTransactionEntityWithAllFields() {
        // Arrange
        String transactionId = "TRX123";
        Date date = new Date();
        Transaction.TypeEnum type = Transaction.TypeEnum.WITHDRAWAL; // Asumiendo que es un enum con este valor
        BigDecimal amount = new BigDecimal("300.25");
        String accountId = "ACC456";

        // Act
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransactionId(transactionId);
        transaction.setDate(date);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setAccountId(accountId);

        // Assert
        assertEquals(transactionId, transaction.getTransactionId());
        assertEquals(date, transaction.getDate());
        assertEquals(type, transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(accountId, transaction.getAccountId());
    }
}