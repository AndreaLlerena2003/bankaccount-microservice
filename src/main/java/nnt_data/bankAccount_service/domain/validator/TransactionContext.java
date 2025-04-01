package nnt_data.bankAccount_service.domain.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.model.Transaction;
/**
 * TransactionContext es una clase que encapsula el contexto de una transacción,
 * incluyendo la entidad de la cuenta y la transacción asociada. Utiliza Lombok
 * para generar automáticamente los métodos getter, setter y el constructor.
 */
@Data
@AllArgsConstructor
public class TransactionContext {
    private final AccountBaseEntity account;
    private final Transaction transaction;
}
