package nnt_data.bankAccount_service.domain.validator;

import reactor.core.publisher.Mono;

public interface Validator<T> {
    Mono<T> validate(T entity);
}
