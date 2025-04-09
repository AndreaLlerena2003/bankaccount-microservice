package nnt_data.bankaccount_microservice.domain.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.model.Transaction;
/**
 * TransactionContext es una clase que encapsula el contexto de una transacción,
 * incluyendo la entidad de la cuenta y la transacción asociada. Utiliza Lombok
 * para generar automáticamente los métodos getter, setter y el constructor.
 */
@Data
@Setter
@AllArgsConstructor
public class TransactionContext {
    private final AccountBaseEntity account;
    private final Transaction transaction;
}
