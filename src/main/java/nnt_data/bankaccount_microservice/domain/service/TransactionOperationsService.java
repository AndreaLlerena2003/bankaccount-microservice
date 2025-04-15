package nnt_data.bankaccount_microservice.domain.service;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.application.port.TransactionOperationsPort;
import nnt_data.bankaccount_microservice.domain.validator.TransactionContext;
import nnt_data.bankaccount_microservice.domain.validator.factory.ValidatorFactory;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.CommissionEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.mapper.TransactionMapper;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.CommissionRepository;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankaccount_microservice.model.Transaction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Date;


/**
/**
 * Servicio que implementa operaciones relacionadas con transacciones bancarias.
 * Proporciona funcionalidades para crear y consultar transacciones entre cuentas.
 */
@Service
@RequiredArgsConstructor
public class TransactionOperationsService implements TransactionOperationsPort {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final BankAccountRepository bankAccountRepository;
    private final ValidatorFactory validatorFactory;
    private final CommissionRepository commissionRepository;

    @Override
    public Mono<Transaction> createTransaction(Transaction transaction) {
        transaction.setDate(new Date());
        if(transaction.getIsByCreditCard() == null){
            transaction.setIsByCreditCard(false);
        }
        return findSourceAccount(transaction.getSourceAccountId())
                .flatMap(sourceAccount -> validateTransaction(sourceAccount, transaction))
                .flatMap(this::processTransaction);
    }

    @Override
    public Flux<Transaction> getTransactions() {
        return transactionRepository.findAll()
                .flatMap(transactionMapper::toDomain)
                .onErrorResume(error -> Flux.error(
                        new RuntimeException("Error al obtener las transacciones", error)));
    }

    @Override
    public Flux<Transaction> getTransactionsAccountId(String accountId) {
        return transactionRepository.findBySourceAccountId(accountId)
                .flatMap(transactionMapper::toDomain)
                .switchIfEmpty(Flux.empty())
                .onErrorResume(error -> Flux.error(
                        new IllegalArgumentException("Error al obtener las transacciones por ID de cuenta", error)));
    }

    private Mono<AccountBaseEntity> findSourceAccount(String accountId) {
        return bankAccountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No existe la cuenta con ID: " + accountId)));
    }

    private Mono<AccountBaseEntity> findDestinyAccount(String accountId) {
        return bankAccountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No existe la cuenta destino con ID: " + accountId)));
    }

    private Mono<Transaction> validateTransaction(AccountBaseEntity account, Transaction transaction) {
        try {
            TransactionContext context = new TransactionContext(account, transaction);
            return validatorFactory.getTransactionValidator(account)
                    .validate(context)
                    .map(result -> transaction);
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException("Tipo de cuenta no soportado"));
        }
    }

    private Mono<Transaction> processTransaction(Transaction transaction) {
        if (transaction.getTransactionMode() == Transaction.TransactionModeEnum.SINGLE_ACCOUNT) {
            return processSingleAccountTransaction(transaction);
        } else {
            return processDualAccountTransaction(transaction);
        }
    }

    private Mono<Transaction> processSingleAccountTransaction(Transaction transaction) {
        return findSourceAccount(transaction.getSourceAccountId())
                .flatMap(account -> processSingleAccountBalance(account, transaction))
                .flatMap(transactionRepository::save)
                .flatMap(transactionMapper::toDomain);
    }

    private Mono<Transaction> processDualAccountTransaction(Transaction transaction) {
        System.out.println("Starting dual account transaction processing for transactionId: " + transaction.getTransactionId());

        return findSourceAccount(transaction.getSourceAccountId())
                .doOnSuccess(sourceAccount -> System.out.println("Source account found: " + sourceAccount.getAccountId()))
                .doOnError(e -> System.err.println("Error finding source account: " + e.getMessage()))
                .flatMap(sourceAccount ->
                        findDestinyAccount(transaction.getDestinyAccountId())
                                .doOnSuccess(destinyAccount -> System.out.println("Destiny account found: " + destinyAccount.getAccountId()))
                                .doOnError(e -> System.err.println("Error finding destiny account: " + e.getMessage()))
                                .flatMap(destinyAccount ->
                                        processDualAccountBalance(sourceAccount, destinyAccount, transaction)
                                                .doOnSuccess(processedTransaction -> System.out.println("Dual account balance processed successfully."))
                                                .doOnError(e -> System.err.println("Error processing dual account balance: " + e.getMessage()))
                                )
                )
                .flatMap(savedTransaction -> {
                    System.out.println("Saving transaction to repository: " + savedTransaction.getTransactionId());
                    return transactionRepository.save(savedTransaction);
                })
                .doOnSuccess(savedTransaction -> System.out.println("Transaction saved successfully: " + savedTransaction.getTransactionId()))
                .doOnError(e -> System.err.println("Error saving transaction: " + e.getMessage()))
                .flatMap(transactionEntity -> {
                    System.out.println("Mapping transaction entity to domain.");
                    return transactionMapper.toDomain(transactionEntity);
                })
                .doOnSuccess(domainTransaction -> System.out.println("Transaction processed successfully: " + domainTransaction.getTransactionId()))
                .doOnError(e -> System.err.println("Error mapping transaction entity to domain: " + e.getMessage()));
    }

    private Mono<TransactionEntity> processSingleAccountBalance(
            AccountBaseEntity account, Transaction transaction) {
        return calculateBalanceWithCommission(account, transaction)
                .flatMap(result -> {
                    BigDecimal newBalance = result.newBalance;
                    BigDecimal commissionAmount = result.commissionApplied;
                    account.setBalance(newBalance);
                    account.setTransactionMovements(account.getTransactionMovements() + 1);

                    Mono<TransactionEntity> saveTransaction = transactionMapper.toEntity(transaction)
                            .flatMap(transactionRepository::save);

                    if (commissionAmount.compareTo(BigDecimal.ZERO) > 0){
                        return saveTransaction
                                .flatMap(savedTransaction -> {
                                    CommissionEntity commission = CommissionEntity.builder()
                                            .transactionId(savedTransaction.getTransactionId())
                                            .accountId(savedTransaction.getSourceAccountId())
                                            .amount(commissionAmount)
                                            .dateTime(savedTransaction.getDate())
                                            .build();

                                    return commissionRepository.save(commission)
                                            .then(bankAccountRepository.save(account))
                                            .thenReturn(savedTransaction);
                                });
                    } else {
                        return bankAccountRepository.save(account)
                                .then(saveTransaction);
                    }
                });
    }

    private Mono<TransactionEntity> processDualAccountBalance(
            AccountBaseEntity sourceAccount, AccountBaseEntity destinyAccount, Transaction transaction) {

        return shouldApplyCommission(sourceAccount)
                .flatMap(needsCommission -> {
                    BigDecimal commissionAmount = needsCommission ?
                            sourceAccount.getFeePerTransaction() : BigDecimal.ZERO;

                    return executeDualAccountTransaction(
                            sourceAccount, destinyAccount, transaction, commissionAmount);
                });
    }

    private Mono<TransactionEntity> executeDualAccountTransaction(
            AccountBaseEntity sourceAccount, AccountBaseEntity destinyAccount,
            Transaction transaction, BigDecimal commissionAmount) {

        if (transaction.getType() == Transaction.TypeEnum.DEPOSIT) {
            return handleDepositTransaction(
                    sourceAccount, destinyAccount, transaction, commissionAmount);
        } else if (transaction.getType() == Transaction.TypeEnum.WITHDRAWAL) {
            return handleWithdrawalTransaction(
                    sourceAccount, destinyAccount, transaction, commissionAmount);
        } else {
            return Mono.error(new IllegalStateException("Tipo de transacción no soportado"));
        }
    }

    private Mono<TransactionEntity> handleDepositTransaction(
            AccountBaseEntity sourceAccount, AccountBaseEntity destinyAccount,
            Transaction transaction, BigDecimal commissionAmount) {

        // Calculate total amount to deduct including commission
        BigDecimal totalDebit = transaction.getAmount().add(commissionAmount);

        // Check if source account has sufficient balance
        if (sourceAccount.getBalance().compareTo(totalDebit) < 0) {
            return Mono.error(new IllegalArgumentException(
                    "Saldo insuficiente para realizar la transacción y pagar la comisión"));
        }

        // Update account balances
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(totalDebit));
        destinyAccount.setBalance(destinyAccount.getBalance().add(transaction.getAmount()));
        sourceAccount.setTransactionMovements(sourceAccount.getTransactionMovements() + 1);

        // Save accounts and create transaction record
        return saveAccountsAndCreateTransaction(sourceAccount, destinyAccount, transaction)
                .flatMap(savedTransaction -> {
                    // Apply commission logic if commission amount is greater than zero
                    if (commissionAmount.compareTo(BigDecimal.ZERO) > 0) {
                        CommissionEntity commission = CommissionEntity.builder()
                                .transactionId(savedTransaction.getTransactionId())
                                .accountId(savedTransaction.getSourceAccountId())
                                .amount(commissionAmount)
                                .dateTime(savedTransaction.getDate())
                                .build();

                        return commissionRepository.save(commission)
                                .thenReturn(savedTransaction);
                    }
                    return Mono.just(savedTransaction);
                });
    }

    private Mono<TransactionEntity> handleWithdrawalTransaction(
            AccountBaseEntity sourceAccount, AccountBaseEntity destinyAccount,
            Transaction transaction, BigDecimal commissionAmount) {

        if (destinyAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            return Mono.error(new IllegalArgumentException("Saldo insuficiente en la cuenta destino"));
        }

        if (sourceAccount.getBalance().compareTo(commissionAmount) < 0) {
            return Mono.error(new IllegalArgumentException("Saldo insuficiente para pagar la comisión"));
        }

        sourceAccount.setBalance(
                sourceAccount.getBalance().add(transaction.getAmount()).subtract(commissionAmount));
        destinyAccount.setBalance(destinyAccount.getBalance().subtract(transaction.getAmount()));
        sourceAccount.setTransactionMovements(sourceAccount.getTransactionMovements() + 1);

        return saveAccountsAndCreateTransaction(sourceAccount, destinyAccount, transaction)
                .flatMap(savedTransaction -> {
                    if (commissionAmount.compareTo(BigDecimal.ZERO) > 0) {
                        CommissionEntity commission = CommissionEntity.builder()
                                .transactionId(savedTransaction.getTransactionId())
                                .accountId(savedTransaction.getSourceAccountId())
                                .amount(commissionAmount)
                                .dateTime(savedTransaction.getDate())
                                .build();

                        return commissionRepository.save(commission)
                                .thenReturn(savedTransaction);
                    }
                    return Mono.just(savedTransaction);
                });
    }

    private Mono<TransactionEntity> saveAccountsAndCreateTransaction(
            AccountBaseEntity sourceAccount, AccountBaseEntity destinyAccount, Transaction transaction) {

        return bankAccountRepository.save(sourceAccount)
                .then(bankAccountRepository.save(destinyAccount))
                .then(transactionMapper.toEntity(transaction));
    }

    private Mono<BalanceResult> calculateBalanceWithCommission(
            AccountBaseEntity account, Transaction transaction) {

        return shouldApplyCommission(account)
                .flatMap(needsCommission -> {
                    BigDecimal initialBalance = account.getBalance();
                    BigDecimal commissionAmount = needsCommission ?
                            account.getFeePerTransaction() : BigDecimal.ZERO;

                    return applyTransactionAmount(
                            initialBalance.subtract(commissionAmount), transaction)
                            .map(newBalance -> new BalanceResult(newBalance, commissionAmount));
                });
    }

    private Mono<Boolean> shouldApplyCommission(AccountBaseEntity account) {
        return Mono.just(account.getTransactionMovements() >= account.getMovementLimit());
    }

    private Mono<BigDecimal> applyTransactionAmount(BigDecimal balance, Transaction transaction) {
        if (transaction.getType() == Transaction.TypeEnum.DEPOSIT) {
            return Mono.just(balance.add(transaction.getAmount()));
        } else if (transaction.getType() == Transaction.TypeEnum.WITHDRAWAL) {
            if (balance.compareTo(transaction.getAmount()) < 0) {
                return Mono.error(new IllegalArgumentException("Saldo insuficiente"));
            }
            return Mono.just(balance.subtract(transaction.getAmount()));
        } else {
            return Mono.error(new IllegalArgumentException("Tipo de transacción no soportado"));
        }
    }

        /**
         * Clase auxiliar para mantener los resultados del cálculo de saldo
         */
        private record BalanceResult(BigDecimal newBalance, BigDecimal commissionApplied) {
    }
}