package nnt_data.bankAccount_service.domain.validator;

import reactor.core.publisher.Mono;
/**
 * Validator es una interfaz genérica que define un método de validación para entidades.
 * Utiliza Reactor Mono para manejar la validación de manera asíncrona.
 *
 * @param <T> El tipo de entidad a validar.
 */
public interface Validator<T> {
    Mono<T> validate(T entity);
}
