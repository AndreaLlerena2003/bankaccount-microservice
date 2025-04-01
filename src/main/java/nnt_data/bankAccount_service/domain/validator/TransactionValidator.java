package nnt_data.bankAccount_service.domain.validator;

import nnt_data.bankAccount_service.domain.validator.TransactionContext;
import nnt_data.bankAccount_service.domain.validator.Validator;
/**
 * TransactionValidator es una interfaz que extiende Validator y proporciona
 * validaciones específicas para contextos de transacciones.
 */
public interface TransactionValidator extends Validator<TransactionContext> {

}
