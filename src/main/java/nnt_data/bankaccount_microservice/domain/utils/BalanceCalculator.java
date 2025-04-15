package nnt_data.bankaccount_microservice.domain.utils;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.TransactionEntity;
import nnt_data.bankaccount_microservice.model.Transaction;

import java.math.BigDecimal;

public class BalanceCalculator {
    public static BigDecimal calculateDailyBalance(BigDecimal currentBalance, Transaction transaction, String accountId) {
        if (transaction.getSourceAccountId().equals(transaction.getDestinyAccountId())) {
            return transaction.getType().equals(Transaction.TypeEnum.DEPOSIT) ?
                    currentBalance.add(transaction.getAmount()) : currentBalance.subtract(transaction.getAmount());
        } else if (transaction.getSourceAccountId().equals(accountId)) {
            return currentBalance.subtract(transaction.getAmount());
        } else if (transaction.getDestinyAccountId().equals(accountId)) {
            return currentBalance.add(transaction.getAmount());
        }
        return currentBalance;
    }
}
