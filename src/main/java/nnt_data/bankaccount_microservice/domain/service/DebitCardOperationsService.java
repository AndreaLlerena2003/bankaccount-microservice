package nnt_data.bankaccount_microservice.domain.service;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.infrastructure.persistence.mapper.DebitCardMapper;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.DebitCardRepository;
import nnt_data.bankaccount_microservice.model.DebitCard;
import nnt_data.bankaccount_microservice.model.Transaction;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;


@RequiredArgsConstructor
@Service
public class DebitCardOperationsService {

    private final DebitCardRepository debitCardRepository;
    private final DebitCardMapper debitCardMapper;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionOperationsService transactionOperationsService;



    public Mono<BigDecimal> getPrimaryAccountBalance(String cardNumber) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "La tarjeta de débito con número " + cardNumber + " no existe")))
                .flatMap(debitCard -> {
                    String primaryAccountId = debitCard.getPrimaryAccountId();
                    return bankAccountRepository.findById(primaryAccountId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                    "La cuenta principal con ID " + primaryAccountId + " no existe")))
                            .map(bankAccount -> bankAccount.getBalance());
                });
    }

    public Mono<DebitCard> createDebitCard(DebitCard debitCard) {
        return bankAccountRepository.findById(debitCard.getPrimaryAccountId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "La cuenta principal con ID " + debitCard.getPrimaryAccountId() + " no existe")))
                .flatMap(primaryAccount -> {
                    if (debitCard.getAssociatedAccountIds() != null && !debitCard.getAssociatedAccountIds().isEmpty()) {
                        return Flux.fromIterable(debitCard.getAssociatedAccountIds())
                                .flatMap(accountId -> bankAccountRepository.findById(accountId)
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                                "La cuenta asociada con ID " + accountId + " no existe"))))
                                .collectList()
                                .then(debitCardMapper.toEntity(debitCard)
                                        .flatMap(debitCardRepository::save)
                                        .flatMap(debitCardMapper::toDomain));
                    } else {
                        if (debitCard.getAssociatedAccountIds() == null) {
                            debitCard.setAssociatedAccountIds(new ArrayList<>());
                        }
                        return debitCardMapper.toEntity(debitCard)
                                .flatMap(debitCardRepository::save)
                                .flatMap(debitCardMapper::toDomain);
                    }
                });
    }

    public Mono<DebitCard> findDebitCardById(String cardId) {
        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "La tarjeta de débito con ID " + cardId + " no existe")))
                .flatMap(debitCardMapper::toDomain);
    }

    public Mono<DebitCard> findDebitCardByNumber(String cardNumber) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "La tarjeta de débito con número " + cardNumber + " no existe")))
                .flatMap(debitCardMapper::toDomain);
    }

    public Mono<DebitCard> associateAccountToCard(String cardId, String accountId) {
        return debitCardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "La tarjeta de débito con ID " + cardId + " no existe")))
                .flatMap(card -> {
                    return bankAccountRepository.findById(accountId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                    "La cuenta con ID " + accountId + " no existe")))
                            .flatMap(account -> {
                                if (card.getPrimaryAccountId().equals(accountId) ||
                                        (card.getAssociatedAccountIds() != null &&
                                                card.getAssociatedAccountIds().contains(accountId))) {
                                    return Mono.error(new IllegalArgumentException(
                                            "La cuenta ya está asociada a esta tarjeta"));
                                }
                                if (card.getAssociatedAccountIds() == null) {
                                    card.setAssociatedAccountIds(java.util.Arrays.asList(accountId));
                                } else {
                                    card.getAssociatedAccountIds().add(accountId);
                                }
                                return debitCardRepository.save(card)
                                        .flatMap(debitCardMapper::toDomain);
                            });
                });
    }

    public Mono<Transaction> processDebitCardTransaction(String cardNumber, Transaction transaction) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "La tarjeta de débito con número " + cardNumber + " no existe")))
                .flatMap(debitCard -> {
                    String primaryAccountId = debitCard.getPrimaryAccountId();
                    Transaction txWithPrimaryAccount = prepareTransaction(transaction, primaryAccountId);

                    return transactionOperationsService.createTransaction(txWithPrimaryAccount)
                            .onErrorResume(e -> {
                                if (e instanceof IllegalArgumentException &&
                                        (e.getMessage().contains("insuficiente") ||
                                                e.getMessage().contains("comisión"))) {

                                    Flux<String> associatedAccountIdsFlux = debitCard.getAssociatedAccountIds() != null ?
                                            Flux.fromIterable(debitCard.getAssociatedAccountIds()) : Flux.empty();

                                    return tryWithAssociatedAccounts(associatedAccountIdsFlux, transaction)
                                            .onErrorResume(err -> Mono.error(new IllegalArgumentException(
                                                    "No hay fondos suficientes en la cuenta principal ni en las asociadas")));
                                }
                                return Mono.error(e);
                            });
                });
    }

    private Transaction prepareTransaction(Transaction baseTransaction, String accountId) {
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(baseTransaction.getAmount());
        newTransaction.setType(baseTransaction.getType());
        newTransaction.setTransactionMode(baseTransaction.getTransactionMode());
        newTransaction.setDate(baseTransaction.getDate());
        newTransaction.setDestinyAccountId(baseTransaction.getDestinyAccountId());
        newTransaction.setSourceAccountId(accountId);
        newTransaction.setIsByCreditCard(true);

        return newTransaction;
    }

    private Mono<Transaction> tryWithAssociatedAccounts(Flux<String> accountIdsFlux, Transaction baseTransaction) {
        return accountIdsFlux
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No hay fondos suficientes en ninguna cuenta asociada")))
                .next()
                .flatMap(currentAccountId -> {
                    Transaction txWithAccount = prepareTransaction(baseTransaction, currentAccountId);

                    return transactionOperationsService.createTransaction(txWithAccount)
                            .onErrorResume(e -> {
                                if ((e instanceof IllegalArgumentException &&
                                        (e.getMessage().contains("insuficiente") ||
                                                e.getMessage().contains("comisión")))) {

                                    return accountIdsFlux.skip(1)
                                            .collectList()
                                            .flatMap(remainingAccounts -> {
                                                if (remainingAccounts.isEmpty()) {
                                                    return Mono.error(new IllegalArgumentException(
                                                            "No hay fondos suficientes en ninguna cuenta asociada a la tarjeta"));
                                                }
                                                return tryWithAssociatedAccounts(Flux.fromIterable(remainingAccounts), baseTransaction);
                                            });
                                }
                                return Mono.error(e);
                            });
                });
    }




}
