package nnt_data.bankAccount_service.application.port;

import nnt_data.bankAccount_service.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionOperationsPort {
    Mono<Transaction> createTransaction(Transaction transaction);
    Flux<Transaction> getTransactions();
    Flux<Transaction> getTransactionsAccountId(String accountId);
}
