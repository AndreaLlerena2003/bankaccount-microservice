package nnt_data.bankaccount_microservice.domain.validator.factory;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.domain.validator.AccountTypeValidator;
import nnt_data.bankaccount_microservice.domain.validator.TransactionValidator;
import nnt_data.bankaccount_microservice.domain.validator.account.CheckingAccountValidator;
import nnt_data.bankaccount_microservice.domain.validator.account.FixedTermAccountValidator;
import nnt_data.bankaccount_microservice.domain.validator.account.SavingsAccountValidator;
import nnt_data.bankaccount_microservice.domain.validator.transaction.CheckingTransactionValidator;
import nnt_data.bankaccount_microservice.domain.validator.transaction.FixedTermTransactionValidator;
import nnt_data.bankaccount_microservice.domain.validator.transaction.SavingsTransactionValidator;
import nnt_data.bankaccount_microservice.model.AccountType;
import org.springframework.stereotype.Component;

/**
 * ValidatorFactory es una fábrica que proporciona validadores específicos para tipos de cuentas
 * y transacciones. Utiliza diferentes implementaciones de AccountTypeValidator y TransactionValidator
 * según el tipo de cuenta.
 */
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
