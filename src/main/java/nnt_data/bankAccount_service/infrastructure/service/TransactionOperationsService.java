package nnt_data.bankAccount_service.infrastructure.service;

import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.infrastructure.persistence.model.AccountBaseEntity;
import nnt_data.bankAccount_service.domain.port.TransactionOperationsPort;
import nnt_data.bankAccount_service.domain.validator.TransactionContext;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.mapper.TransactionMapper;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankAccount_service.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TransactionOperationsService implements TransactionOperationsPort {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BankAccountRepository bankAccountRepository;
    private final ValidatorFactory validatorFactory;

    @Override
    public Mono<Transaction> createTransaction(Transaction transaction) {
        transaction.setDate(new Date());
        return bankAccountRepository.findById(transaction.getAccountId())
                .doOnNext(account -> System.out.println("Cuenta encontrada: " + account))
                .switchIfEmpty(Mono.error(new IllegalStateException(
                        "No existe la cuenta con ID: " + transaction.getAccountId())))
                .flatMap(account -> {
                    System.out.println("Iniciando validación de transacción para cuenta tipo: " + account.getAccountType());
                    return validateTransaction(account, transaction);
                })
                .doOnNext(validatedTx -> System.out.println("Transacción validada exitosamente"))
                .flatMap(validatedTx -> {
                    System.out.println("Iniciando procesamiento de transacción");
                    return processTransaction(validatedTx);
                })
                .doOnNext(processedTx -> System.out.println("Transacción procesada exitosamente: " + processedTx))
                .doOnError(error -> System.err.println("Error en la transacción: " + error.getMessage()));
    }

    @Override
    public Flux<Transaction> getTransactions() {
        return transactionRepository.findAll()
                .flatMap(transactionMapper::toDomain)
                .onErrorResume(error -> Flux.error(new RuntimeException("Error al obtener las transacciones")));
    }

    @Override
    public Flux<Transaction> getTransactionsAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId)
                .flatMap(entity -> transactionMapper.toDomain(entity))
                .switchIfEmpty(Flux.empty())
                .onErrorResume(error -> Flux.error(new RuntimeException("Error al obtener las transacciones por ID de cuenta")));
    }

    private Mono<Transaction> validateTransaction(AccountBaseEntity account, Transaction transaction) {
        try {
            TransactionContext context = new TransactionContext(account, transaction);
            return validatorFactory.getTransactionValidator(account)
                    .validate(context)
                    .map(result -> transaction);
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalStateException("Tipo de cuenta no soportado"));
        }
    }

    private Mono<Transaction> processTransaction(Transaction transaction) {
        return bankAccountRepository.findById(transaction.getAccountId())
                .flatMap(account -> {
                    BigDecimal newBalance;
                    if (transaction.getType() == Transaction.TypeEnum.DEPOSIT) {
                        newBalance = account.getBalance().add(transaction.getAmount());
                    } else {
                        if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
                            return Mono.error(new IllegalStateException("Saldo insuficiente"));
                        }
                        newBalance = account.getBalance().subtract(transaction.getAmount());
                    }
                    account.setBalance(newBalance);
                    return bankAccountRepository.save(account)
                            .then(transactionMapper.toEntity(transaction));
                })
                .flatMap(transactionRepository::save)
                .flatMap(transactionMapper::toDomain)
                .doOnError(error -> Mono.error(new RuntimeException("Error al procesar la transacción: " + error.getMessage())));
    }
}