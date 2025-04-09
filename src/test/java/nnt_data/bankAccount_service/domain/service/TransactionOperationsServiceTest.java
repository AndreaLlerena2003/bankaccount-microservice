package nnt_data.bankAccount_service.domain.service;

import nnt_data.bankAccount_service.application.port.TransactionOperationsPort;
import nnt_data.bankAccount_service.domain.validator.TransactionContext;
import nnt_data.bankAccount_service.domain.validator.TransactionValidator;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankAccount_service.infrastructure.persistence.mapper.TransactionMapper;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankAccount_service.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionOperationsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private ValidatorFactory validatorFactory;

    @Mock
    private TransactionValidator transactionValidator;

    @InjectMocks
    private TransactionOperationsService transactionOperationsService;

    private Transaction transaction;
    private TransactionEntity transactionEntity;
    private AccountBaseEntity accountBaseEntity;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setAccountId("123");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(Transaction.TypeEnum.DEPOSIT);


        transactionEntity = new TransactionEntity();
        transactionEntity.setAccountId("123");
        transactionEntity.setAmount(new BigDecimal("100.00"));
        transactionEntity.setType(Transaction.TypeEnum.DEPOSIT);


        accountBaseEntity = new AccountBaseEntity();
        accountBaseEntity.setAccountId("123");
        accountBaseEntity.setBalance(new BigDecimal("200.00"));
        accountBaseEntity.setTransactionMovements(5);
        accountBaseEntity.setMovementLimit(10);
        accountBaseEntity.setFeePerTransaction(new BigDecimal("5.00"));
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        when(bankAccountRepository.findById("123")).thenReturn(Mono.just(accountBaseEntity));
        when(validatorFactory.getTransactionValidator(any(AccountBaseEntity.class))).thenReturn(transactionValidator);
        when(transactionValidator.validate(any(TransactionContext.class))).thenReturn(Mono.just(new TransactionContext(accountBaseEntity, transaction)));
        when(bankAccountRepository.save(any(AccountBaseEntity.class))).thenReturn(Mono.just(accountBaseEntity));
        when(transactionMapper.toEntity(any(Transaction.class))).thenReturn(Mono.just(transactionEntity));
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));

        // Act
        Mono<Transaction> result = transactionOperationsService.createTransaction(transaction);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(t ->
                        t.getAccountId().equals("123") &&
                                t.getAmount().compareTo(new BigDecimal("100.00")) == 0 &&
                                t.getType() == Transaction.TypeEnum.DEPOSIT)
                .verifyComplete();

        verify(bankAccountRepository, times(2)).findById("123");
        verify(bankAccountRepository).save(any(AccountBaseEntity.class));
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_AccountNotFound() {
        // Arrange
        when(bankAccountRepository.findById("123")).thenReturn(Mono.empty());

        // Act
        Mono<Transaction> result = transactionOperationsService.createTransaction(transaction);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof IllegalStateException &&
                                e.getMessage().contains("No existe la cuenta con ID: 123"))
                .verify();
    }

    @Test
    void createTransaction_InsufficientBalance() {
        // Arrange
        transaction.setType(Transaction.TypeEnum.WITHDRAWAL);
        transaction.setAmount(new BigDecimal("300.00")); // Mayor que el saldo

        when(bankAccountRepository.findById("123")).thenReturn(Mono.just(accountBaseEntity));
        when(validatorFactory.getTransactionValidator(any(AccountBaseEntity.class))).thenReturn(transactionValidator);
        when(transactionValidator.validate(any(TransactionContext.class))).thenReturn(Mono.just(new TransactionContext(accountBaseEntity, transaction)));

        // Act
        Mono<Transaction> result = transactionOperationsService.createTransaction(transaction);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e ->
                        e instanceof RuntimeException &&
                                e.getMessage().contains("Error al procesar la transacción") &&
                                e.getMessage().contains("Saldo insuficiente"))
                .verify();
    }

    @Test
    void createTransaction_WithCommission() {
        // Arrange
        accountBaseEntity.setTransactionMovements(10); 

        when(bankAccountRepository.findById("123")).thenReturn(Mono.just(accountBaseEntity));
        when(validatorFactory.getTransactionValidator(any(AccountBaseEntity.class))).thenReturn(transactionValidator);
        when(transactionValidator.validate(any(TransactionContext.class))).thenReturn(Mono.just(new TransactionContext(accountBaseEntity,transaction)));
        when(bankAccountRepository.save(any(AccountBaseEntity.class))).thenReturn(Mono.just(accountBaseEntity));
        when(transactionMapper.toEntity(any(Transaction.class))).thenReturn(Mono.just(transactionEntity));
        when(transactionRepository.save(any(TransactionEntity.class))).thenReturn(Mono.just(transactionEntity));
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));

        // Act
        Mono<Transaction> result = transactionOperationsService.createTransaction(transaction);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(t -> t.getAccountId().equals("123"))
                .verifyComplete();

        // Verificar que se actualiza el saldo restando la comisión
        verify(bankAccountRepository).save(argThat(account ->
                account.getBalance().compareTo(new BigDecimal("295.00")) == 0)); // 200 + 100 - 5 (comisión)
    }

    @Test
    void getTransactions_Success() {
        // Arrange
        when(transactionRepository.findAll()).thenReturn(Flux.just(transactionEntity));
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));

        // Act
        Flux<Transaction> result = transactionOperationsService.getTransactions();

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(t -> t.getAccountId().equals("123"))
                .verifyComplete();

        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactionsAccountId_Success() {
        // Arrange
        when(transactionRepository.findByAccountId("123")).thenReturn(Flux.just(transactionEntity));
        when(transactionMapper.toDomain(any(TransactionEntity.class))).thenReturn(Mono.just(transaction));

        // Act
        Flux<Transaction> result = transactionOperationsService.getTransactionsAccountId("123");

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(t -> t.getAccountId().equals("123"))
                .verifyComplete();

        verify(transactionRepository).findByAccountId("123");
    }

    @Test
    void getTransactionsAccountId_NoTransactionsFound() {
        // Arrange
        when(transactionRepository.findByAccountId("123")).thenReturn(Flux.empty());

        // Act
        Flux<Transaction> result = transactionOperationsService.getTransactionsAccountId("123");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(transactionRepository).findByAccountId("123");
    }
}