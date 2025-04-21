package nnt_data.bankaccount_microservice.domain.service;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.domain.utils.BalanceCalculator;
import nnt_data.bankaccount_microservice.domain.utils.DateUtils;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.CommissionEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankaccount_microservice.infrastructure.persistence.mapper.TransactionMapper;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.BankAccountRepository;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.CommissionRepository;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.DebitCardRepository;
import nnt_data.bankaccount_microservice.infrastructure.persistence.repository.TransactionRepository;
import nnt_data.bankaccount_microservice.model.AccountResume;
import nnt_data.bankaccount_microservice.model.CommissionReport;
import nnt_data.bankaccount_microservice.model.Transaction;
import nnt_data.bankaccount_microservice.model.TransactionReport;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportingService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CommissionRepository commissionRepository;
    private final TransactionMapper transactionMapper;
    private final DebitCardRepository debitCardRepository;

    public Flux<AccountResume> generateResumeOfAvarageBalance(String customerId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        return bankAccountRepository.findByCustomerId(customerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encontraron cuentas para el cliente ID: " + customerId)))
                .flatMap(account ->
                        transactionRepository.findBySourceAccountIdOrDestinyAccountIdAndDateBetween(
                                        account.getAccountId(),
                                        account.getAccountId(),
                                        DateUtils.toDate(startOfMonth),
                                        DateUtils.toDate(endOfMonth)
                                )
                                .collectList()
                                .map(transactions -> {
                                    BigDecimal averageBalance = calculateSalaryAverage(
                                            account.getBalance(),
                                            transactions,
                                            startOfMonth,
                                            endOfMonth,
                                            account.getAccountId()
                                    );
                                    return new AccountResume(account.getAccountId(), account.getAccountType(), averageBalance);
                                })
                )
                .onErrorResume(e -> {
                    System.out.println("Error generating resume of average balance: " + e.getMessage());
                    return Mono.error(new IllegalArgumentException("Error al obtener las cuentas del cliente: " + e.getMessage(), e));
                });
    }

    private BigDecimal calculateSalaryAverage(BigDecimal initialBalance, List<TransactionEntity> transactions,
                                              LocalDate startOfMonth, LocalDate endOfMonth, String accountId) {
        BigDecimal dailyBalance = initialBalance;
        BigDecimal sumOfBalances = BigDecimal.ZERO;
        LocalDate currentDate = startOfMonth;

        while (!currentDate.isAfter(endOfMonth)) {
            LocalDate finalCurrentDate = currentDate;
            List<TransactionEntity> dailyTransactions = transactions.stream()
                    .filter(transaction -> DateUtils.toLocalDate(transaction.getDate()).equals(finalCurrentDate))
                    .toList();

            for (TransactionEntity transactionEntity : dailyTransactions) {
                Transaction transaction = transactionMapper.toDomain(transactionEntity).block();
                if (transaction != null) {
                    dailyBalance = BalanceCalculator.calculateDailyBalance(dailyBalance, transaction, accountId);
                }
            }

            sumOfBalances = sumOfBalances.add(dailyBalance);
            currentDate = currentDate.plusDays(1);
        }

        long daysInPeriod = startOfMonth.until(endOfMonth, ChronoUnit.DAYS) + 1;
        return sumOfBalances.divide(BigDecimal.valueOf(daysInPeriod), RoundingMode.HALF_UP);
    }

    public Mono<AccountResume> generateResumeOfAvarageBalanceForPeriodByAccountId(String accountId, Date startDate, Date endDate) {
        LocalDate startLocalDate = DateUtils.toLocalDate(startDate);
        LocalDate endLocalDate = DateUtils.toLocalDate(endDate);

        return bankAccountRepository.findById(accountId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No se encontró la cuenta con ID: " + accountId)))
                .flatMap(account ->
                        transactionRepository.findBySourceAccountIdOrDestinyAccountIdAndDateBetween(
                                        account.getAccountId(),
                                        account.getAccountId(),
                                        startDate,
                                        endDate
                                )
                                .collectList()
                                .map(transactions -> {
                                    BigDecimal averageBalance = calculateSalaryAverage(
                                            account.getBalance(),
                                            transactions,
                                            startLocalDate,
                                            endLocalDate,
                                            account.getAccountId()
                                    );
                                    return new AccountResume(account.getAccountId(), account.getAccountType(), averageBalance);
                                })
                )
                .onErrorResume(e -> {
                    System.out.println("Error generating resume of average balance for account ID: " + e.getMessage());
                    return Mono.error(new IllegalArgumentException("Error al obtener el resumen de la cuenta: " + e.getMessage(), e));
                });
    }

    public Mono<CommissionReport> generateCommissionReportByAccountId(String accountId, Date startDate, Date endDate) {
        LocalDate startLocalDate = DateUtils.toLocalDate(startDate);
        LocalDate endLocalDate = DateUtils.toLocalDate(endDate);

        Date startOfDayDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDayDate = Date.from(endLocalDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        return commissionRepository.findByAccountIdAndDateTimeBetween(accountId, startOfDayDate, endOfDayDate)
                .collectList()
                .flatMap(commissions -> {
                    BigDecimal totalCommission = commissions.stream()
                            .map(CommissionEntity::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    CommissionReport report = new CommissionReport();
                    report.setAccountId(accountId);
                    report.setTotalCommission(totalCommission);
                    report.setTransactionCount((long) commissions.size());
                    report.setStartDate(startDate);
                    report.setEndOfDate(endDate);
                    return Mono.just(report);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    CommissionReport emptyReport = new CommissionReport();
                    emptyReport.setAccountId(accountId);
                    emptyReport.setTotalCommission(BigDecimal.ZERO);
                    emptyReport.setTransactionCount(0L);
                    emptyReport.setStartDate(startDate);
                    emptyReport.setEndOfDate(endDate);
                    return Mono.just(emptyReport);
                }));
    }

    public Mono<TransactionReport> getLastTenTransactions(String cardNumber) {
        return debitCardRepository.findByCardNumber(cardNumber)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "La tarjeta de débito con número " + cardNumber + " no existe")))
                .flatMap(debitCard -> {
                    List<String> allAccountIds = new ArrayList<>();
                    allAccountIds.add(debitCard.getPrimaryAccountId());
                    if (debitCard.getAssociatedAccountIds() != null && !debitCard.getAssociatedAccountIds().isEmpty()) {
                        allAccountIds.addAll(debitCard.getAssociatedAccountIds());
                    }
                    return Flux.fromIterable(allAccountIds)
                            .flatMap(accountId ->
                                    Flux.concat(
                                            transactionRepository.findBySourceAccountId(accountId)
                                    )
                            )
                            .filter(transaction -> Boolean.TRUE.equals(transaction.getIsByCreditCard()))
                            .sort(Comparator.comparing(transaction ->
                                            transaction.getDate() != null ? transaction.getDate() : new Date(0),
                                    Comparator.reverseOrder()))
                            .take(10)
                            .collectList()
                            .flatMap(transactions ->
                                    Flux.fromIterable(transactions)
                                            .flatMap(transactionMapper::toDomain)
                                            .collectList()
                                            .map(domainTransactions -> {
                                                TransactionReport report = new TransactionReport();
                                                report.setCardNumber(cardNumber);
                                                report.setGenerationDate(new Date());
                                                report.setTransactions(domainTransactions);
                                                report.setTransactionCount(domainTransactions.size());
                                                if (!domainTransactions.isEmpty()) {
                                                    report.setNewestTransactionDate(domainTransactions.get(0).getDate());
                                                    report.setOldestTransactionDate(domainTransactions.get(domainTransactions.size() - 1).getDate());
                                                }
                                                return report;
                                            })
                            );
                });
    }

}
