package nnt_data.bankAccount_service.domain.validator;


import reactor.core.publisher.Mono;

/**
 * TransactionValidator es una interfaz que extiende Validator y proporciona
 * validaciones específicas para contextos de transacciones.
 */
public interface TransactionValidator extends Validator<TransactionContext> {

}
