package nnt_data.bankaccount_microservice.domain.utils;

import nnt_data.bankaccount_microservice.model.Transaction;

import java.math.BigDecimal;

public class BalanceCalculator {
    public static BigDecimal calculateDailyBalance(BigDecimal currentBalance, Transaction transaction, String accountId) {
        if (transaction.getTransactionMode() == Transaction.TransactionModeEnum.SINGLE_ACCOUNT) {
            if (transaction.getType().equals(Transaction.TypeEnum.DEPOSIT)) {
                return currentBalance.add(transaction.getAmount());
            } else if (transaction.getType().equals(Transaction.TypeEnum.WITHDRAWAL)) {
                return currentBalance.subtract(transaction.getAmount());
            }
            return currentBalance;
        } else if (transaction.getTransactionMode() == Transaction.TransactionModeEnum.INTER_ACCOUNT) {
            if(transaction.getType().equals(Transaction.TypeEnum.DEPOSIT)) {
                if(transaction.getSourceAccountId().equals(accountId)) {
                    return currentBalance.add(transaction.getAmount());
                } else if(transaction.getDestinyAccountId().equals(accountId)) {
                    return currentBalance.subtract(transaction.getAmount());
                }
            } else if (transaction.getType().equals(Transaction.TypeEnum.WITHDRAWAL)) {
                if(transaction.getSourceAccountId().equals(accountId)) {
                    return currentBalance.subtract(transaction.getAmount());
                } else if(transaction.getDestinyAccountId().equals(accountId)) {
                    return currentBalance.add(transaction.getAmount());
                }
            }
        }
        return currentBalance;
    }
}