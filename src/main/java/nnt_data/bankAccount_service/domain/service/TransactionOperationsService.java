package nnt_data.bankAccount_service.domain.service;

import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.application.port.TransactionOperationsPort;
import nnt_data.bankAccount_service.domain.validator.TransactionContext;
import nnt_data.bankAccount_service.domain.validator.factory.ValidatorFactory;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankAccount_service.infrastructure.persistence.mapper.TransactionMapper;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankAccount_service.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankAccount_service.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;


/**
 * AccountOperationsService es un servicio que implementa AccountOperationsPort y proporciona
 * operaciones para la gestión de cuentas bancarias. Utiliza estrategias de creación y actualización
 * de cuentas basadas en el tipo de cliente, así como un repositorio para la persistencia de datos.
 */
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
                .switchIfEmpty(Mono.error(new IllegalStateException(
                        "No existe la cuenta con ID: " + transaction.getAccountId())))
                .flatMap(account -> validateTransaction(account, transaction))
                .flatMap(this::processTransaction);
    }

    private Mono<Boolean> needsToPayCommission(AccountBaseEntity accountBaseEntity) {
        if(accountBaseEntity.getTransactionMovements() >= accountBaseEntity.getMovementLimit()){
            return Mono.just(true);
        } else {
            return Mono.just(false);
        }
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
                .flatMap(transactionMapper::toDomain)
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
                .flatMap(account -> processTransactionForAccount(transaction, account))
                .flatMap(transactionRepository::save)
                .flatMap(transactionMapper::toDomain)
                .onErrorMap(error -> new RuntimeException("Error al procesar la transacción: " + error.getMessage()));
    }

    private Mono<TransactionEntity> processTransactionForAccount(Transaction transaction, AccountBaseEntity account) {
        return calculateNewBalance(account, transaction)
                .flatMap(newBalance -> updateAccountAndCreateTransactionEntity(account, newBalance, transaction));
    }

    private Mono<BigDecimal> calculateNewBalance(AccountBaseEntity account, Transaction transaction) {
        return needsToPayCommission(account)
                .map(needsCommission -> {
                    BigDecimal newBalance = account.getBalance();
                    if (needsCommission) {
                        newBalance = newBalance.subtract(account.getFeePerTransaction());
                    }
                    return newBalance;
                })
                .flatMap(balance -> applyTransactionAmount(balance, transaction));
    }

    private Mono<BigDecimal> applyTransactionAmount(BigDecimal balance,Transaction transaction) {
        if (transaction.getType() == Transaction.TypeEnum.DEPOSIT) {
            return Mono.just(balance.add(transaction.getAmount()));
        } else {
            if (balance.compareTo(transaction.getAmount()) < 0) {
                return Mono.error(new IllegalStateException("Saldo insuficiente"));
            }
            return Mono.just(balance.subtract(transaction.getAmount()));
        }
    }

    private Mono<TransactionEntity> updateAccountAndCreateTransactionEntity(
            AccountBaseEntity account, BigDecimal newBalance, Transaction transaction) {
        account.setBalance(newBalance);
        account.setTransactionMovements(account.getTransactionMovements() + 1);
        return bankAccountRepository.save(account)
                .then(transactionMapper.toEntity(transaction));
    }

}