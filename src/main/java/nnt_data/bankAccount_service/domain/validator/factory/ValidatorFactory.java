package nnt_data.bankAccount_service.domain.validator.factory;

import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.infrastructure.persistence.model.AccountBaseEntity;
import nnt_data.bankAccount_service.domain.validator.AccountTypeValidator;
import nnt_data.bankAccount_service.domain.validator.TransactionValidator;
import nnt_data.bankAccount_service.domain.validator.account.CheckingAccountValidator;
import nnt_data.bankAccount_service.domain.validator.account.FixedTermAccountValidator;
import nnt_data.bankAccount_service.domain.validator.account.SavingsAccountValidator;
import nnt_data.bankAccount_service.domain.validator.transaction.CheckingTransactionValidator;
import nnt_data.bankAccount_service.domain.validator.transaction.FixedTermTransactionValidator;
import nnt_data.bankAccount_service.domain.validator.transaction.SavingsTransactionValidator;
import nnt_data.bankAccount_service.model.AccountType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidatorFactory {

    private final SavingsAccountValidator savingsAccountValidator;
    private final FixedTermAccountValidator fixedTermAccountValidator;
    private final CheckingAccountValidator checkingAccountValidator;

    private final SavingsTransactionValidator savingsTransactionValidator;
    private final FixedTermTransactionValidator fixedTermTransactionValidator;
    private final CheckingTransactionValidator checkingTransactionValidator;

    public AccountTypeValidator getAccountValidator(AccountType accountType) {
        switch (accountType) {
            case SAVINGS:
                return savingsAccountValidator;
            case FIXED_TERM:
                return fixedTermAccountValidator;
            case CHECKING:
                return checkingAccountValidator;
            default:
                throw new IllegalArgumentException("Tipo de cuenta no soportado: " + accountType);
        }
    }

    public TransactionValidator getTransactionValidator(AccountBaseEntity accountBaseEntity) {
        switch (accountBaseEntity.getAccountType()){
            case SAVINGS:
                return savingsTransactionValidator;
            case FIXED_TERM:
                return fixedTermTransactionValidator;
            case CHECKING:
                return checkingTransactionValidator;
            default:
                throw new IllegalArgumentException("Tipo de cuenta no soportado: " + accountBaseEntity.getAccountType());
        }

    }


}
