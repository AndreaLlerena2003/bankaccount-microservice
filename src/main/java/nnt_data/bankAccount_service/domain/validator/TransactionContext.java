package nnt_data.bankAccount_service.domain.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.model.Transaction;

@Data
@AllArgsConstructor
public class TransactionContext {
    private final AccountBaseEntity account;
    private final Transaction transaction;
}
