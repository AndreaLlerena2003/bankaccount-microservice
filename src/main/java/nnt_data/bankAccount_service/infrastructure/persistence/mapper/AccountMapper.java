package nnt_data.bankAccount_service.infrastructure.persistence.mapper;

import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.model.AccountBase;
import reactor.core.publisher.Mono;
/**
 * AccountMapper es una interfaz que define métodos para mapear entre entidades de cuenta
 * y objetos de dominio. Utiliza Reactor Mono para manejar el mapeo de manera asíncrona.
 */
public interface AccountMapper {
    Mono<AccountBaseEntity> toEntity(AccountBase account);
    Mono<AccountBase> toDomain(AccountBaseEntity accountBaseEntity);
}
